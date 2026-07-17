package com.copy9029.bangumimanagerreformed.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.YearMonth


@Entity(tableName = "bangumi_items")
data class Bangumi(
    @PrimaryKey(autoGenerate = true)
    val bangumiId: Int = 1,

    val title: String,
    val seasonYear: Int,
    val seasonMonth: Int,
    val myScore: Int?,   // 0-100, divided by 10 when displayed
    val themeColorLong: Long,

    val firstBroadcastDate: LocalDate,
    val totalEpisodes: Int? = null,

    val latestWatchedEpisode: Int = 0, // 0表示未观看  TODO:（不考虑第0话）
    val isActive: Boolean = true,

    // 冗余字段：
    val expectedEndDate: LocalDate? = null, // totalEpisodes改变时 / schedule改变时：更新

)


@Entity(
    tableName = "bangumi_schedule_items",
    primaryKeys = ["bangumiId", "episodeId"],
    foreignKeys = [
        ForeignKey(
            entity = Bangumi::class,
            parentColumns = ["bangumiId"],
            childColumns = ["bangumiId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BangumiSchedule(
    val bangumiId: Int,
    val episodeId: Int,

    val broadcastDate: LocalDate,

)