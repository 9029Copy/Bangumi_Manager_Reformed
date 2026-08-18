package com.copy9029.bangumimanagerreformed.data.backup

import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.CalendarInactiveVisibilityDefaults
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeParseException

internal object BackupValidator {
    fun validateStructure(root: JSONObject) {
        root.requireKeys(
            sectionName = "备份文件",
            "formatVersion",
            "exportedAt",
            "appVersion",
            "bangumis",
            "schedules",
            "calendarSettings",
            "globalSettings",
        )
        val formatVersion = root.getInt("formatVersion")
        requireBackup(BackupFormat.isSupported(formatVersion)) {
            "不支持的备份格式版本：$formatVersion"
        }

        val bangumis = root.getJSONArray("bangumis")
        repeat(bangumis.length()) { index ->
            val bangumi = bangumis.getJSONObject(index)
            bangumi.requireKeys(
                sectionName = "第 ${index + 1} 个 Bangumi 项目",
                "bangumiId",
                "title",
                "seasonYear",
                "seasonMonth",
                "myScore",
                "firstBroadcastDate",
                "totalEpisodes",
                "latestWatchedEpisode",
                "isActive",
            )
            val modifiedAtKey = if (formatVersion == 1) {
                "lastBasicInfoModifiedAtMillis"
            } else {
                "lastModifiedAtMillis"
            }
            bangumi.requireKeys(
                sectionName = "第 ${index + 1} 个 Bangumi 项目",
                modifiedAtKey,
            )
        }

        val schedules = root.getJSONArray("schedules")
        repeat(schedules.length()) { index ->
            schedules.getJSONObject(index).requireKeys(
                sectionName = "第 ${index + 1} 个 Schedule 项目",
                "bangumiId",
                "episodeId",
                "broadcastDate",
            )
        }

        root.getJSONObject("calendarSettings").requireKeys(
            sectionName = "日历设置",
            "calendarInactiveVisibility",
            "calendarFinishedEpisodeVisible",
            "calendarFinishedBangumiVisible",
            "calendarWeeksBeforeCurrent",
            "calendarWeeksAfterCurrent",
            "calendarWeeksPrefix",
        )
        root.getJSONObject("globalSettings").requireKeys(
            sectionName = "全局设置",
            "appThemeMode",
            "default01ColorLong",
            "default04ColorLong",
            "default07ColorLong",
            "default10ColorLong",
        )
    }

    fun validateContent(root: JSONObject, backup: BackupImportData) {
        requireBackup(BackupFormat.isSupported(root.getInt("formatVersion"))) {
            "不支持的备份格式版本：${root.getInt("formatVersion")}"
        }
        try {
            Instant.parse(root.getString("exportedAt"))
        } catch (exception: DateTimeParseException) {
            throw BackupValidationException("导出时间格式不正确", exception)
        }
        requireBackup(root.getString("appVersion").isNotBlank()) {
            "应用版本不能为空"
        }

        val bangumiIds = backup.bangumis.map(Bangumi::bangumiId)
        requireBackup(bangumiIds.distinct().size == bangumiIds.size) {
            "Bangumi ID 不能重复"
        }

        backup.bangumis.forEach { bangumi ->
            val itemName = "Bangumi ${bangumi.bangumiId}"
            requireBackup(bangumi.bangumiId > 0) { "$itemName 的 ID 必须大于 0" }
            requireBackup(bangumi.title.isNotBlank()) { "$itemName 的标题不能为空" }
            requireBackup(
                bangumi.seasonYear in BackupFormat.MIN_SEASON_YEAR..BackupFormat.MAX_SEASON_YEAR,
            ) { "$itemName 的季度年份超出范围" }
            requireBackup(bangumi.seasonMonth in BackupFormat.SEASON_MONTHS) {
                "$itemName 的季度月份必须是 1、4、7 或 10"
            }
            requireBackup(bangumi.myScore == null || bangumi.myScore in 0..100) {
                "$itemName 的评分必须在 0 到 100 之间"
            }
            requireBackup(bangumi.totalEpisodes == null || bangumi.totalEpisodes > 0) {
                "$itemName 的总集数必须大于 0"
            }
            requireBackup(bangumi.latestWatchedEpisode >= 0) {
                "$itemName 的已观看集数不能小于 0"
            }
            requireBackup(
                bangumi.totalEpisodes == null ||
                    bangumi.latestWatchedEpisode <= bangumi.totalEpisodes,
            ) { "$itemName 的已观看集数不能大于总集数" }
            requireBackup(bangumi.lastModifiedAtMillis >= 0L) {
                "$itemName 的最近更改时间不能小于 0"
            }
        }

        val bangumiById = backup.bangumis.associateBy(Bangumi::bangumiId)
        val scheduleKeys = mutableSetOf<Pair<Int, Int>>()
        backup.schedules.forEach { schedule ->
            requireBackup(schedule.bangumiId in bangumiById) {
                "Schedule 引用了不存在的 Bangumi ID：${schedule.bangumiId}"
            }
            requireBackup(schedule.episodeId > 0) {
                "Bangumi ${schedule.bangumiId} 的 Schedule 集数必须大于 0"
            }
            requireBackup(scheduleKeys.add(schedule.bangumiId to schedule.episodeId)) {
                "Bangumi ${schedule.bangumiId} 的第 ${schedule.episodeId} 集 Schedule 重复"
            }

            val totalEpisodes = bangumiById.getValue(schedule.bangumiId).totalEpisodes
            requireBackup(totalEpisodes == null || schedule.episodeId <= totalEpisodes) {
                "Bangumi ${schedule.bangumiId} 的 Schedule 集数不能大于总集数"
            }
        }

        val schedulesByBangumiId = backup.schedules.groupBy(BangumiSchedule::bangumiId)
        backup.bangumis.forEach { bangumi ->
            val firstSchedule = schedulesByBangumiId[bangumi.bangumiId]
                ?.firstOrNull { schedule -> schedule.episodeId == 1 }
                ?: throw BackupValidationException(
                    "Bangumi ${bangumi.bangumiId} 缺少第 1 集 Schedule",
                )
            requireBackup(firstSchedule.broadcastDate == bangumi.firstBroadcastDate) {
                "Bangumi ${bangumi.bangumiId} 的第 1 集日期必须与开播日期一致"
            }
        }

        val calendarSettings = backup.calendarSettings
        requireBackup(
            calendarSettings.calendarInactiveVisibility in INACTIVE_VISIBILITY_VALUES,
        ) { "日历的活跃状态筛选值无效" }
        requireBackup(
            calendarSettings.calendarWeeksBeforeCurrent in
                BackupFormat.MIN_CALENDAR_WEEKS..BackupFormat.MAX_CALENDAR_WEEKS,
        ) { "日历向前显示的周数超出范围" }
        requireBackup(
            calendarSettings.calendarWeeksAfterCurrent in
                BackupFormat.MIN_CALENDAR_WEEKS..BackupFormat.MAX_CALENDAR_WEEKS,
        ) { "日历向后显示的周数超出范围" }
        requireBackup(
            calendarSettings.calendarWeeksPrefix in 0..BackupFormat.MAX_CALENDAR_WEEKS_PREFIX,
        ) { "日历周数前缀超出范围" }

        with(backup.globalSettings) {
            validateColorLong("1 月默认颜色", default01ColorLong)
            validateColorLong("4 月默认颜色", default04ColorLong)
            validateColorLong("7 月默认颜色", default07ColorLong)
            validateColorLong("10 月默认颜色", default10ColorLong)
        }
    }

    private fun JSONObject.requireKeys(sectionName: String, vararg keys: String) {
        keys.forEach { key ->
            requireBackup(has(key)) { "$sectionName 缺少必要字段：$key" }
        }
    }

    private fun validateColorLong(name: String, value: Long) {
        requireBackup(value in BackupFormat.MIN_ARGB_COLOR_LONG..BackupFormat.MAX_ARGB_COLOR_LONG) {
            "$name 不是有效的 ARGB 色值"
        }
    }

    private inline fun requireBackup(condition: Boolean, message: () -> String) {
        if (!condition) throw BackupValidationException(message())
    }

    private val INACTIVE_VISIBILITY_VALUES = setOf(
        CalendarInactiveVisibilityDefaults.ACTIVE,
        CalendarInactiveVisibilityDefaults.ALL,
        CalendarInactiveVisibilityDefaults.INACTIVE,
    )
}
