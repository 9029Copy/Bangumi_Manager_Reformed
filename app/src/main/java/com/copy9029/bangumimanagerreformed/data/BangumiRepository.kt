package com.copy9029.bangumimanagerreformed.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BangumiRepository @Inject constructor(
    private val bangumiDao: BangumiDao,
) {

//    suspend fun insertBangumi(bangumi: Bangumi): Long = bangumiDao.insertBangumi(bangumi)
//
//    suspend fun updateBangumi(bangumi: Bangumi) = bangumiDao.updateBangumi(bangumi)
//
//    suspend fun deleteBangumi(bangumiId: Int) = bangumiDao.deleteBangumi(bangumiId)

//    fun getLatestAiredEpisode(bangumiId: Int) =


    //=============== ViewModel Operations ==================

    fun getAllBangumis(): Flow<List<Bangumi>> {
        return bangumiDao.getAllBangumis()
    }

    fun getAllSchedules(): Flow<List<BangumiSchedule>> {
        return bangumiDao.getAllSchedules()
    }

    suspend fun watch1Episode(bangumiId: Int) {

    }

}