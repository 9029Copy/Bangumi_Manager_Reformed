package com.copy9029.bangumimanagerreformed.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

const val INACTIVE_COLOR_LONG = 0xFFADADADL     // grey


data class BangumiAddInfo(
    val seasonYear: Int,
    val seasonMonth: Int,
    val title: String,
    val firstBroadcastDate: LocalDate,
)

@Entity(tableName = "bangumi_items")
data class Bangumi(
    @PrimaryKey(autoGenerate = true)
    val bangumiId: Int = 0,

    val title: String,
    val seasonYear: Int,
    val seasonMonth: Int,
    val myScore: Int?,          // 0-100, divided by 10 when displayed

    val firstBroadcastDate: LocalDate,
    val totalEpisodes: Int? = null,

    val latestWatchedEpisode: Int = 0,      // 0 表示未观看
    val isActive: Boolean = true,

    val lastModifiedAtMillis: Long = 0L,

    // 冗余字段：
    val expectedEndDate: LocalDate? = null, // totalEpisodes 或 schedule 改变时更新

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
