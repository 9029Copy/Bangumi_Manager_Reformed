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

    // ================= 番剧主表 (Bangumi) 操作 =================

    // 【增】插入一部番剧。返回 Long 表示新插入行的 rowId (即自动生成的 bangumiId)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBangumi(bangumi: Bangumi): Long

    // 【改】更新番剧信息 (根据主键 bangumiId 匹配)
    @Update
    suspend fun updateBangumi(bangumi: Bangumi)

    // 【删】删除一部番剧 (注意：因为外键配置了 CASCADE，关联的 Schedule 会自动删除)
    @Delete
    suspend fun deleteBangumi(bangumi: Bangumi)

    // 查操作 TODO:待添加项
    // 【查】获取所有番剧，按首播日期降序排列 (最新的在前面)
    @Query("SELECT * FROM bangumi_items ORDER BY firstBroadcastDate DESC")
    fun getAllBangumis(): Flow<List<Bangumi>>

    // 【查】根据 ID 获取单部番剧
    @Query("SELECT * FROM bangumi_items WHERE bangumiId = :id")
    fun getBangumiById(id: Int): Flow<Bangumi?>

    // 【查】根据季度 (YearMonth) 筛选番剧（YearMonth 对象会自动调用 TypeConverter 转换）
//    @Query("SELECT * FROM bangumi_items WHERE season = :season")    // TODO: to be completed
//    fun getBangumisBySeason(season: YearMonth): Flow<List<Bangumi>>

    // 【查】根据更新状态筛选番剧

    // 【查】获取所有已完结的番剧
//    @Query("SELECT * FROM bangumi_items WHERE isEnded = 1")
//    fun getEndedBangumis(): Flow<List<Bangumi>>


    // ================= 放送时间表 (BangumiSchedule) 操作 =================

    // 【增】批量插入放送日程 (比如一次性导入一季的所有集数)
    @Insert(onConflict = OnConflictStrategy.IGNORE) // 如果该集已经存在，则忽略
    suspend fun insertSchedules(schedules: List<BangumiSchedule>)

    // 【查】获取某部番剧的所有放送日程，按日期升序
    @Query("SELECT * FROM bangumi_schedule_items WHERE bangumiId = :bangumiId ORDER BY broadcastDate ASC")
    fun getSchedulesByBangumiId(bangumiId: Int): Flow<List<BangumiSchedule>>

    // 【查】获取某一天放送的所有剧集 (跨番剧查询，常用于做“今日放送”功能)
    @Query("SELECT * FROM bangumi_schedule_items WHERE broadcastDate = :date ORDER BY broadcastDate ASC")
    fun getSchedulesByDate(date: LocalDate): Flow<List<BangumiSchedule>>

    // 【查】获取某部番剧最新已放送的一集 (用于和 latestWatchedEpisode 对比，看是否追平)
    @Query("SELECT MAX(episodeId) FROM bangumi_schedule_items WHERE bangumiId = :bangumiId AND broadcastDate <= :currentDate")
    suspend fun getLatestAiredEpisodeId(bangumiId: Int, currentDate: Long): Int?

}