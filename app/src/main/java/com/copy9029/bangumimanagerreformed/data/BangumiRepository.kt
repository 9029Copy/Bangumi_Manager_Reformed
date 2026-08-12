package com.copy9029.bangumimanagerreformed.data

import androidx.room.withTransaction
import com.copy9029.bangumimanagerreformed.util.calculateExpectedEndDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BangumiRepository @Inject constructor(
    private val database: AppDatabase,
    private val bangumiDao: BangumiDao,
) {
    private val watchedEpisodeSetMutex = Mutex()

    suspend fun updateBangumi(newBangumi: Bangumi, oldBangumi: Bangumi) {
        val totalEpisodesChanged = oldBangumi.totalEpisodes != newBangumi.totalEpisodes
        val expectedEndDate = if (totalEpisodesChanged) {
            val schedules = bangumiDao.getSchedulesByBangumiIdOnce(newBangumi.bangumiId)
            newBangumi.calculateExpectedEndDate(schedules)
        } else {
            oldBangumi.expectedEndDate
        }

        val bangumiToUpdate = newBangumi.copy(
            lastBasicInfoModifiedAtMillis = if (oldBangumi.hasSameBasicInfoAs(newBangumi)) {
                oldBangumi.lastBasicInfoModifiedAtMillis
            } else {
                System.currentTimeMillis()
            },
            expectedEndDate = expectedEndDate,
        )

        bangumiDao.updateBangumi(bangumiToUpdate)
    }

    suspend fun updateBangumiAndSchedules(
        newBangumi: Bangumi,
        oldBangumi: Bangumi,
        schedules: List<BangumiSchedule>?,
    ): Bangumi {
        require(newBangumi.bangumiId == oldBangumi.bangumiId) {
            "新旧 Bangumi 必须指向同一个项目"
        }
        require(
            schedules != null ||
                newBangumi.firstBroadcastDate == oldBangumi.firstBroadcastDate
        ) {
            "保留旧 Schedule 时不能修改开播日期"
        }
        schedules?.let { validateSchedules(newBangumi, it) }

        return database.withTransaction {
            val effectiveSchedules = if (schedules == null) {
                bangumiDao.getSchedulesByBangumiIdOnce(newBangumi.bangumiId)
            } else {
                syncSchedules(
                    bangumiId = newBangumi.bangumiId,
                    schedules = schedules,
                )
                schedules
            }
            val bangumiToUpdate = newBangumi.copy(
                lastBasicInfoModifiedAtMillis = if (oldBangumi.hasSameBasicInfoAs(newBangumi)) {
                    oldBangumi.lastBasicInfoModifiedAtMillis
                } else {
                    System.currentTimeMillis()
                },
                expectedEndDate = newBangumi.calculateExpectedEndDate(
                    effectiveSchedules
                ),
            )

            bangumiDao.updateBangumi(bangumiToUpdate)
            bangumiToUpdate
        }
    }

    private suspend fun insertOrUpdateSchedule(schedule: BangumiSchedule) {
        bangumiDao.insertSchedule(schedule)
        refreshExpectedEndDate(schedule.bangumiId)
    }

    private suspend fun deleteSchedule(bangumiId: Int, episodeId: Int) {
        if (episodeId <= 1) return

        bangumiDao.deleteSchedule(bangumiId, episodeId)
        refreshExpectedEndDate(bangumiId)
    }


    //=============== ViewModel Operations ==================

    fun getAllBangumis(): Flow<List<Bangumi>> {
        return bangumiDao.getAllBangumis()
    }

    fun getBangumiById(bangumiId: Int): Flow<Bangumi?> {
        return bangumiDao.getBangumiById(bangumiId)
    }

    fun getAllSchedules(): Flow<List<BangumiSchedule>> {
        return bangumiDao.getAllSchedules()
    }

    suspend fun getSchedulesByBangumiIdOnce(
        bangumiId: Int,
    ): List<BangumiSchedule> {
        return bangumiDao.getSchedulesByBangumiIdOnce(bangumiId)
    }

    suspend fun watchedEpisodeAdd(bangumiId: Int, num: Int) {
        bangumiDao.addWatchedEpisode(bangumiId, num)
    }

    suspend fun watchedEpisodeSet(bangumiId: Int, newNum: Int) {
        watchedEpisodeSetMutex.withLock {
            bangumiDao.setWatchedEpisode(bangumiId, newNum)
        }
    }

    suspend fun addNewBangumi(info: BangumiAddInfo) {
        database.withTransaction {
            addNewBangumiInTransaction(info)
        }
    }

    suspend fun addNewBangumisBatch(infos: List<BangumiAddInfo>) {
        database.withTransaction {
            infos.forEach { info ->
                addNewBangumiInTransaction(info)
            }
        }
    }

    private suspend fun addNewBangumiInTransaction(info: BangumiAddInfo) {
        val bangumi = Bangumi(
            title = info.title,
            seasonYear = info.seasonYear,
            seasonMonth = info.seasonMonth,
            myScore = null,
            firstBroadcastDate = info.firstBroadcastDate,
            totalEpisodes = null,
            latestWatchedEpisode = 0,
            isActive = true,
            lastBasicInfoModifiedAtMillis = System.currentTimeMillis(),
            expectedEndDate = null,
        )
        val id = bangumiDao.insertBangumi(bangumi)

        val schedule = BangumiSchedule(
            bangumiId = id.toInt(),
            episodeId = 1,
            broadcastDate = info.firstBroadcastDate
        )
        insertOrUpdateSchedule(schedule)
    }

    suspend fun toggleBangumiActive(bangumiId: Int) {
        bangumiDao.toggleBangumiActive(bangumiId)
    }

    suspend fun deleteBangumi(bangumiId: Int) {
        bangumiDao.deleteBangumiById(bangumiId)
    }



    // ========================= private ===============================

    private fun validateSchedules(
        bangumi: Bangumi,
        schedules: List<BangumiSchedule>,
    ) {
        require(schedules.isNotEmpty()) {
            "Schedule 列表必须包含第 1 集锚点"
        }
        require(schedules.all { it.bangumiId == bangumi.bangumiId }) {
            "Schedule 列表中存在不属于目标番剧的锚点"
        }
        require(schedules.all { it.episodeId > 0 }) {
            "Schedule 集数必须是大于 0 的整数"
        }
        require(schedules.map(BangumiSchedule::episodeId).distinct().size == schedules.size) {
            "Schedule 集数不能重复"
        }

        val firstSchedule = schedules.firstOrNull { it.episodeId == 1 }
        require(firstSchedule != null) {
            "Schedule 列表必须包含第 1 集锚点"
        }
        require(firstSchedule.broadcastDate == bangumi.firstBroadcastDate) {
            "第 1 集锚点日期必须与开播日期一致"
        }
    }

    private suspend fun syncSchedules(
        bangumiId: Int,
        schedules: List<BangumiSchedule>,
    ) {
        val storedByEpisode = bangumiDao.getSchedulesByBangumiIdOnce(bangumiId)
            .associateBy(BangumiSchedule::episodeId)
        val updatedByEpisode = schedules.associateBy(BangumiSchedule::episodeId)

        schedules.filter { schedule ->
            storedByEpisode[schedule.episodeId] != schedule
        }.forEach { schedule ->
            bangumiDao.insertSchedule(schedule)
        }
        (storedByEpisode.keys - updatedByEpisode.keys).forEach { episodeId ->
            bangumiDao.deleteSchedule(
                bangumiId = bangumiId,
                episodeId = episodeId,
            )
        }
    }

    private suspend fun refreshExpectedEndDate(bangumiId: Int) {
        val bangumi = bangumiDao.getBangumiByIdOnce(bangumiId)
            ?: return
        val schedules = bangumiDao.getSchedulesByBangumiIdOnce(bangumiId)
        val expectedEndDate = bangumi.calculateExpectedEndDate(schedules)

        if (bangumi.expectedEndDate != expectedEndDate) {
            bangumiDao.updateBangumi(
                bangumi.copy(expectedEndDate = expectedEndDate)
            )
        }
    }

}

private fun Bangumi.hasSameBasicInfoAs(other: Bangumi): Boolean {
    return title == other.title &&
            seasonYear == other.seasonYear &&
            seasonMonth == other.seasonMonth &&
            myScore == other.myScore &&
            firstBroadcastDate == other.firstBroadcastDate &&
            totalEpisodes == other.totalEpisodes &&
            isActive == other.isActive
}
