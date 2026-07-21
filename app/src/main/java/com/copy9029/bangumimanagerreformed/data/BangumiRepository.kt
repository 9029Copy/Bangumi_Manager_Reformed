package com.copy9029.bangumimanagerreformed.data

import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BangumiRepository @Inject constructor(
    private val bangumiDao: BangumiDao,
) {



    suspend fun updateBangumi(bangumi: Bangumi) {
        bangumiDao.updateBangumi(bangumi)
    }

    private suspend fun insertSchedule(schedule: BangumiSchedule) {
        bangumiDao.insertSchedule(schedule)
    }

    private suspend fun deleteSchedule(bangumiId: Int, episodeId: Int) {

    }


    //=============== ViewModel Operations ==================

    fun getAllBangumis(): Flow<List<Bangumi>> {
        return bangumiDao.getAllBangumis()
    }

    fun getAllSchedules(): Flow<List<BangumiSchedule>> {
        return bangumiDao.getAllSchedules()
    }

    suspend fun watchedEpisodeAdd(bangumiId: Int, num: Int) {
        val bangumi = bangumiDao.getBangumiById(bangumiId)
        if (bangumi != null) {
            val newNum = bangumi.latestWatchedEpisode + num
            if (newNum >= 0) {
                if ((bangumi.totalEpisodes == null) || (newNum <= bangumi.totalEpisodes)) {
                    updateBangumi(
                        bangumi.copy(latestWatchedEpisode = newNum)
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
            expectedEndDate = null,
        )
        val id = bangumiDao.insertBangumi(bangumi)

        val schedule = BangumiSchedule(
            bangumiId = id.toInt(),
            episodeId = 1,
            broadcastDate = info.firstBroadcastDate
        )
        insertSchedule(schedule)
    }

    suspend fun toggleBangumiActive(bangumiId: Int) {
        val bangumi = bangumiDao.getBangumiById(bangumiId)
        if (bangumi != null) {
            val oldActive = bangumi.isActive
            updateBangumi(
                bangumi.copy(isActive = !oldActive)
            )
        }
    }

    suspend fun deleteBangumi(bangumiId: Int) {
        bangumiDao.deleteBangumiById(bangumiId)
    }


}