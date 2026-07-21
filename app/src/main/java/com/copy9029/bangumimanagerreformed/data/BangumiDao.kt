package com.copy9029.bangumimanagerreformed.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

@Dao
interface BangumiDao {

    @Query("SELECT * FROM bangumi_items ORDER BY firstBroadcastDate DESC")
    fun getAllBangumis(): Flow<List<Bangumi>>

    @Query("SELECT * FROM bangumi_items WHERE bangumiId = :bangumiId")
    suspend fun getBangumiById(bangumiId: Int): Bangumi?

    @Query("""
        SELECT * FROM bangumi_schedule_items
        ORDER BY bangumiId ASC, episodeId ASC
    """)
    fun getAllSchedules(): Flow<List<BangumiSchedule>>

//    @Query("SELECT * FROM theme_color_items ORDER BY seasonMonth ASC")  // TODO: themeColor
//    fun getAllThemeColors(): Flow<List<ThemeColor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBangumi(bangumi: Bangumi): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BangumiSchedule)

    @Update
    suspend fun updateBangumi(bangumi: Bangumi)

    @Query("DELETE FROM bangumi_items WHERE bangumiId = :bangumiId")
    suspend fun deleteBangumiById(bangumiId: Int)



    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 已确认操作 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    // ===========================================================
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 未确认操作 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓


    // =================== 番剧主表 (Bangumi) 操作 =========================



//
//
//
//
//
//    // ================= 放送时间表 (BangumiSchedule) 操作 ====================
//
//    // 【增】批量插入放送日程 (比如一次性导入一季的所有集数)
//    @Insert(onConflict = OnConflictStrategy.IGNORE) // 如果该集已经存在，则忽略
//    suspend fun insertSchedules(schedules: List<BangumiSchedule>)   // FIXME
//
//    // 【查】获取某部番剧的所有放送日程，按日期升序
//    @Query("SELECT * FROM bangumi_schedule_items WHERE bangumiId = :bangumiId ORDER BY broadcastDate ASC")
//    fun getSchedulesByBangumiId(bangumiId: Int): Flow<List<BangumiSchedule>>    // FIXME
//
//    // 【查】获取某一天放送的所有剧集 (跨番剧查询，常用于做“今日放送”功能)
//    @Query("SELECT * FROM bangumi_schedule_items WHERE broadcastDate = :date ORDER BY broadcastDate ASC")
//    fun getSchedulesByDate(date: LocalDate): Flow<List<BangumiSchedule>>    // FIXME
//
//    // 【查】获取某部番剧最新已放送的一集 (用于和 latestWatchedEpisode 对比，看是否追平)
//    @Query("SELECT MAX(episodeId) FROM bangumi_schedule_items WHERE bangumiId = :bangumiId AND broadcastDate <= :currentDate")
//    suspend fun getLatestAiredEpisodeId(bangumiId: Int, currentDate: Long): Int?    // FIXME

}