package com.copy9029.bangumimanagerreformed.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

val themeColorByMonth = mapOf(
    1 to 0xFFFFFFAAL,
    4 to 0xFFB3FFB3L,
    7 to 0xFF80FFFFL,
    10 to 0xFFFF9191L,
)

data class BangumiAddInfo(
    val seasonYear: Int,
    val seasonMonth: Int,
    val themeColorLong: Long,
    val title: String,
    val firstBroadcastDate: LocalDate,
)

@Entity(tableName = "bangumi_items")
data class Bangumi(
    @PrimaryKey(autoGenerate = true)
    val bangumiId: Int = 0,     // 基本信息：自动生成

    val title: String,          // 基本信息
    val seasonYear: Int,        // 基本信息
    val seasonMonth: Int,       // 基本信息
    val myScore: Int?,          // 基本信息：0-100, divided by 10 when displayed
    val themeColorLong: Long,   // 基本信息

    val firstBroadcastDate: LocalDate,      // 日期信息
    val totalEpisodes: Int? = null,         // 基本信息

    val latestWatchedEpisode: Int = 0,      // 观看信息：0表示未观看  TODO:（不考虑第0话）
    val isActive: Boolean = true,           // 基本信息

    val lastBasicInfoModifiedAtMillis: Long = 0L, // 系统信息：最近修改基本信息的时刻

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


//@Entity(tableName = "theme_color_items")  // TODO: themeColor
//data class ThemeColor(
//    @PrimaryKey(autoGenerate = false)
//    val seasonMonth: Int = 99,  // 1, 4, 7, 10  /  99(unspecified)
//
//    val themeColorLong: Long,
//)
