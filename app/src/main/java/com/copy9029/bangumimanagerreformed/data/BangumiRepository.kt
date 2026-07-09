package com.copy9029.bangumimanagerreformed.data

class BangumiRepository(private val bangumiDao: BangumiDao) {

    suspend fun insertBangumi(bangumi: Bangumi): Long = bangumiDao.insertBangumi(bangumi)

    suspend fun updateBangumi(bangumi: Bangumi) = bangumiDao.updateBangumi(bangumi)

    suspend fun deleteBangumi(bangumi: Bangumi) = bangumiDao.deleteBangumi(bangumi)



}