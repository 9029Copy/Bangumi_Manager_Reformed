package com.copy9029.bangumimanagerreformed.data

import com.copy9029.bangumimanagerreformed.util.calculateExpectedEndDate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BangumiRepository @Inject constructor(
    private val bangumiDao: BangumiDao,
) {

    suspend fun updateBangumi(newBangumi: Bangumi, oldBangumi: Bangumi) {
        val totalEpisodesChanged = oldBangumi.totalEpisodes != newBangumi.totalEpisodes
        val expectedEndDate = if (totalEpisodesChanged) {
            val schedules = bangumiDao.getSchedulesByBangumiId(newBangumi.bangumiId)
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

    suspend fun getSchedulesByBangumiId(
        bangumiId: Int,
    ): List<BangumiSchedule> {
        return bangumiDao.getSchedulesByBangumiId(bangumiId)
    }

    suspend fun watchedEpisodeAdd(bangumiId: Int, num: Int) {
        val bangumi = bangumiDao.getBangumiByIdOnce(bangumiId)
        if (bangumi != null) {
            val newNum = bangumi.latestWatchedEpisode + num
            if (newNum >= 0) {
                if ((bangumi.totalEpisodes == null) || (newNum <= bangumi.totalEpisodes)) {
                    updateBangumi(
                        newBangumi = bangumi.copy(latestWatchedEpisode = newNum),
                        oldBangumi = bangumi,
                    )
                }
            }
        }
    }

    suspend fun watchedEpisodeSet(bangumiId: Int, newNum: Int) {
        val bangumi = bangumiDao.getBangumiByIdOnce(bangumiId)
        if (bangumi != null) {
            if (newNum >= 0) {
                if ((bangumi.totalEpisodes == null) || (newNum <= bangumi.totalEpisodes)) {
                    updateBangumi(
                        newBangumi = bangumi.copy(latestWatchedEpisode = newNum),
                        oldBangumi = bangumi,
                    )
                }
            }
        }
    }

//    fun getAllThemeColors(): Flow<List<ThemeColor>> {  // TODO: themeColor
//        return bangumiDao.getAllThemeColors()
//    }

    suspend fun addNewBangumi(info: BangumiAddInfo) {
        val bangumi = Bangumi(
            title = info.title,
            seasonYear = info.seasonYear,
            seasonMonth = info.seasonMonth,
            myScore = null,
            themeColorLong = info.themeColorLong,
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

    suspend fun addNewBangumisBatch(infos: List<BangumiAddInfo>) {
        infos.forEach { info ->
            addNewBangumi(info)
        }
    }

    suspend fun toggleBangumiActive(bangumiId: Int) {
        val bangumi = bangumiDao.getBangumiByIdOnce(bangumiId)
        if (bangumi != null) {
            val oldActive = bangumi.isActive
            updateBangumi(
                newBangumi = bangumi.copy(isActive = !oldActive),
                oldBangumi = bangumi,
            )
        }
    }

    suspend fun deleteBangumi(bangumiId: Int) {
        bangumiDao.deleteBangumiById(bangumiId)
    }



    // ========================= private ===============================

    private suspend fun refreshExpectedEndDate(bangumiId: Int) {
        val bangumi = bangumiDao.getBangumiByIdOnce(bangumiId)
            ?: return
        val schedules = bangumiDao.getSchedulesByBangumiId(bangumiId)
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
            themeColorLong == other.themeColorLong &&
            totalEpisodes == other.totalEpisodes &&
            isActive == other.isActive
}
