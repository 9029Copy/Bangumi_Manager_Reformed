package com.copy9029.bangumimanagerreformed.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BangumiDao {

    @Query("SELECT * FROM bangumi_items ORDER BY firstBroadcastDate DESC")
    fun getAllBangumis(): Flow<List<Bangumi>>

    @Query("SELECT * FROM bangumi_items WHERE bangumiId = :bangumiId")
    suspend fun getBangumiByIdOnce(bangumiId: Int): Bangumi?

    @Query("SELECT * FROM bangumi_items WHERE bangumiId = :bangumiId")
    fun getBangumiById(bangumiId: Int): Flow<Bangumi?>

    @Query("""
        SELECT * FROM bangumi_schedule_items
        ORDER BY bangumiId ASC, episodeId ASC
    """)
    fun getAllSchedules(): Flow<List<BangumiSchedule>>

    @Query("""
        SELECT * FROM bangumi_schedule_items
        WHERE bangumiId = :bangumiId
        ORDER BY episodeId ASC
    """)
    suspend fun getSchedulesByBangumiIdOnce(bangumiId: Int): List<BangumiSchedule>

//    @Query("SELECT * FROM theme_color_items ORDER BY seasonMonth ASC")  // TODO: themeColor
//    fun getAllThemeColors(): Flow<List<ThemeColor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBangumi(bangumi: Bangumi): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BangumiSchedule)

    @Query("""
        DELETE FROM bangumi_schedule_items
        WHERE bangumiId = :bangumiId AND episodeId = :episodeId
    """)
    suspend fun deleteSchedule(bangumiId: Int, episodeId: Int)

    @Update
    suspend fun updateBangumi(bangumi: Bangumi)

    @Query("DELETE FROM bangumi_items WHERE bangumiId = :bangumiId")
    suspend fun deleteBangumiById(bangumiId: Int)


}
