package com.copy9029.bangumimanagerreformed.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class BangumiSeasonYearRange(
    val minYear: Int?,
    val maxYear: Int?,
)

@Dao
interface BangumiDao {

    @Query("SELECT * FROM bangumi_items ORDER BY firstBroadcastDate DESC")
    fun getAllBangumis(): Flow<List<Bangumi>>

    @Query("""
        SELECT * FROM bangumi_items
        WHERE seasonYear = :seasonYear AND seasonMonth = :seasonMonth
        ORDER BY firstBroadcastDate ASC, bangumiId ASC
    """)
    fun getBangumisBySeason(
        seasonYear: Int,
        seasonMonth: Int,
    ): Flow<List<Bangumi>>

    @Query("""
        SELECT MIN(seasonYear) AS minYear, MAX(seasonYear) AS maxYear
        FROM bangumi_items
    """)
    fun getBangumiSeasonYearRange(): Flow<BangumiSeasonYearRange>

    @Query("SELECT * FROM bangumi_items ORDER BY bangumiId ASC")
    suspend fun getAllBangumisOnce(): List<Bangumi>

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
        ORDER BY bangumiId ASC, episodeId ASC
    """)
    suspend fun getAllSchedulesOnce(): List<BangumiSchedule>

    @Query("""
        SELECT * FROM bangumi_schedule_items
        WHERE bangumiId = :bangumiId
        ORDER BY episodeId ASC
    """)
    suspend fun getSchedulesByBangumiIdOnce(bangumiId: Int): List<BangumiSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBangumi(bangumi: Bangumi): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBangumis(bangumis: List<Bangumi>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BangumiSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<BangumiSchedule>)

    @Query("DELETE FROM bangumi_schedule_items")
    suspend fun deleteAllSchedules()

    @Query("DELETE FROM bangumi_items")
    suspend fun deleteAllBangumis()

    @Query("""
        DELETE FROM bangumi_schedule_items
        WHERE bangumiId = :bangumiId AND episodeId = :episodeId
    """)
    suspend fun deleteSchedule(bangumiId: Int, episodeId: Int)

    @Update
    suspend fun updateBangumi(bangumi: Bangumi)

    @Query("""
        UPDATE bangumi_items
        SET latestWatchedEpisode = latestWatchedEpisode + :delta
        WHERE bangumiId = :bangumiId
          AND latestWatchedEpisode + :delta >= 0
          AND (
              totalEpisodes IS NULL
              OR latestWatchedEpisode + :delta <= totalEpisodes
          )
    """)
    suspend fun addWatchedEpisode(bangumiId: Int, delta: Int): Int

    @Query("""
        UPDATE bangumi_items
        SET latestWatchedEpisode = :episode
        WHERE bangumiId = :bangumiId
          AND :episode >= 0
          AND (totalEpisodes IS NULL OR :episode <= totalEpisodes)
    """)
    suspend fun setWatchedEpisode(bangumiId: Int, episode: Int): Int

    @Query("""
        UPDATE bangumi_items
        SET isActive = NOT isActive,
            lastModifiedAtMillis = :modifiedAtMillis
        WHERE bangumiId = :bangumiId
    """)
    suspend fun toggleBangumiActive(bangumiId: Int, modifiedAtMillis: Long): Int

    @Query("DELETE FROM bangumi_items WHERE bangumiId = :bangumiId")
    suspend fun deleteBangumiById(bangumiId: Int)


}
