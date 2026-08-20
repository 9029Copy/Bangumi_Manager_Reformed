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
            section = BackupSection.Root,
            "formatVersion",
            "exportedAt",
            "appVersion",
            "bangumis",
            "schedules",
            "calendarSettings",
            "globalSettings",
        )
        val formatVersion = root.getInt("formatVersion")
        requireBackup(
            condition = BackupFormat.isSupported(formatVersion),
            issue = BackupValidationIssue.UnsupportedFormatVersion(formatVersion),
        )

        val bangumis = root.getJSONArray("bangumis")
        repeat(bangumis.length()) { index ->
            val section = BackupSection.BangumiItem(index + 1)
            val bangumi = bangumis.getJSONObject(index)
            bangumi.requireKeys(
                section = section,
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
            bangumi.requireKeys(section = section, modifiedAtKey)
        }

        val schedules = root.getJSONArray("schedules")
        repeat(schedules.length()) { index ->
            schedules.getJSONObject(index).requireKeys(
                section = BackupSection.ScheduleItem(index + 1),
                "bangumiId",
                "episodeId",
                "broadcastDate",
            )
        }

        root.getJSONObject("calendarSettings").requireKeys(
            section = BackupSection.CalendarSettings,
            "calendarInactiveVisibility",
            "calendarFinishedEpisodeVisible",
            "calendarFinishedBangumiVisible",
            "calendarWeeksBeforeCurrent",
            "calendarWeeksAfterCurrent",
            "calendarWeeksPrefix",
        )
        root.getJSONObject("globalSettings").requireKeys(
            section = BackupSection.GlobalSettings,
            "appThemeMode",
            "default01ColorLong",
            "default04ColorLong",
            "default07ColorLong",
            "default10ColorLong",
        )
    }

    fun validateContent(root: JSONObject, backup: BackupImportData) {
        val formatVersion = root.getInt("formatVersion")
        requireBackup(
            condition = BackupFormat.isSupported(formatVersion),
            issue = BackupValidationIssue.UnsupportedFormatVersion(formatVersion),
        )
        try {
            Instant.parse(root.getString("exportedAt"))
        } catch (exception: DateTimeParseException) {
            throw BackupValidationException(
                issue = BackupValidationIssue.InvalidExportedAt,
                cause = exception,
            )
        }
        requireBackup(
            condition = root.getString("appVersion").isNotBlank(),
            issue = BackupValidationIssue.BlankAppVersion,
        )

        val bangumiIds = backup.bangumis.map(Bangumi::bangumiId)
        requireBackup(
            condition = bangumiIds.distinct().size == bangumiIds.size,
            issue = BackupValidationIssue.DuplicateBangumiId,
        )

        backup.bangumis.forEach { bangumi ->
            fun issue(reason: BangumiValidationReason) =
                BackupValidationIssue.InvalidBangumi(bangumi.bangumiId, reason)

            requireBackup(bangumi.bangumiId > 0, issue(BangumiValidationReason.ID_NON_POSITIVE))
            requireBackup(bangumi.title.isNotBlank(), issue(BangumiValidationReason.TITLE_BLANK))
            requireBackup(
                bangumi.seasonYear in BackupFormat.MIN_SEASON_YEAR..BackupFormat.MAX_SEASON_YEAR,
                issue(BangumiValidationReason.SEASON_YEAR_OUT_OF_RANGE),
            )
            requireBackup(
                bangumi.seasonMonth in BackupFormat.SEASON_MONTHS,
                issue(BangumiValidationReason.SEASON_MONTH_INVALID),
            )
            requireBackup(
                bangumi.myScore == null || bangumi.myScore in 0..100,
                issue(BangumiValidationReason.SCORE_OUT_OF_RANGE),
            )
            requireBackup(
                bangumi.totalEpisodes == null || bangumi.totalEpisodes > 0,
                issue(BangumiValidationReason.TOTAL_EPISODES_NON_POSITIVE),
            )
            requireBackup(
                bangumi.latestWatchedEpisode >= 0,
                issue(BangumiValidationReason.WATCHED_EPISODE_NEGATIVE),
            )
            requireBackup(
                bangumi.totalEpisodes == null ||
                    bangumi.latestWatchedEpisode <= bangumi.totalEpisodes,
                issue(BangumiValidationReason.WATCHED_EPISODE_EXCEEDS_TOTAL),
            )
            requireBackup(
                bangumi.lastModifiedAtMillis >= 0L,
                issue(BangumiValidationReason.LAST_MODIFIED_NEGATIVE),
            )
        }

        val bangumiById = backup.bangumis.associateBy(Bangumi::bangumiId)
        val scheduleKeys = mutableSetOf<Pair<Int, Int>>()
        backup.schedules.forEach { schedule ->
            fun issue(reason: ScheduleValidationReason) = BackupValidationIssue.InvalidSchedule(
                bangumiId = schedule.bangumiId,
                episodeId = schedule.episodeId,
                reason = reason,
            )

            requireBackup(
                schedule.bangumiId in bangumiById,
                issue(ScheduleValidationReason.MISSING_BANGUMI_REFERENCE),
            )
            requireBackup(
                schedule.episodeId > 0,
                issue(ScheduleValidationReason.EPISODE_NON_POSITIVE),
            )
            requireBackup(
                scheduleKeys.add(schedule.bangumiId to schedule.episodeId),
                issue(ScheduleValidationReason.DUPLICATE),
            )

            val totalEpisodes = bangumiById.getValue(schedule.bangumiId).totalEpisodes
            requireBackup(
                totalEpisodes == null || schedule.episodeId <= totalEpisodes,
                issue(ScheduleValidationReason.EPISODE_EXCEEDS_TOTAL),
            )
        }

        val schedulesByBangumiId = backup.schedules.groupBy(BangumiSchedule::bangumiId)
        backup.bangumis.forEach { bangumi ->
            val firstSchedule = schedulesByBangumiId[bangumi.bangumiId]
                ?.firstOrNull { schedule -> schedule.episodeId == 1 }
                ?: throw BackupValidationException(
                    BackupValidationIssue.MissingFirstSchedule(bangumi.bangumiId),
                )
            requireBackup(
                firstSchedule.broadcastDate == bangumi.firstBroadcastDate,
                BackupValidationIssue.FirstScheduleDateMismatch(bangumi.bangumiId),
            )
        }

        val calendarSettings = backup.calendarSettings
        requireBackup(
            calendarSettings.calendarInactiveVisibility in INACTIVE_VISIBILITY_VALUES,
            BackupValidationIssue.InvalidCalendarSetting(
                CalendarSettingValidationReason.INACTIVE_VISIBILITY_INVALID,
            ),
        )
        requireBackup(
            calendarSettings.calendarWeeksBeforeCurrent in
                BackupFormat.MIN_CALENDAR_WEEKS..BackupFormat.MAX_CALENDAR_WEEKS,
            BackupValidationIssue.InvalidCalendarSetting(
                CalendarSettingValidationReason.WEEKS_BEFORE_OUT_OF_RANGE,
            ),
        )
        requireBackup(
            calendarSettings.calendarWeeksAfterCurrent in
                BackupFormat.MIN_CALENDAR_WEEKS..BackupFormat.MAX_CALENDAR_WEEKS,
            BackupValidationIssue.InvalidCalendarSetting(
                CalendarSettingValidationReason.WEEKS_AFTER_OUT_OF_RANGE,
            ),
        )
        requireBackup(
            calendarSettings.calendarWeeksPrefix in 0..BackupFormat.MAX_CALENDAR_WEEKS_PREFIX,
            BackupValidationIssue.InvalidCalendarSetting(
                CalendarSettingValidationReason.WEEKS_PREFIX_OUT_OF_RANGE,
            ),
        )

        with(backup.globalSettings) {
            validateColorLong(month = 1, value = default01ColorLong)
            validateColorLong(month = 4, value = default04ColorLong)
            validateColorLong(month = 7, value = default07ColorLong)
            validateColorLong(month = 10, value = default10ColorLong)
        }
    }

    private fun JSONObject.requireKeys(section: BackupSection, vararg keys: String) {
        keys.forEach { key ->
            requireBackup(
                condition = has(key),
                issue = BackupValidationIssue.MissingField(section, key),
            )
        }
    }

    private fun validateColorLong(month: Int, value: Long) {
        requireBackup(
            condition = value in
                BackupFormat.MIN_ARGB_COLOR_LONG..BackupFormat.MAX_ARGB_COLOR_LONG,
            issue = BackupValidationIssue.InvalidColor(month),
        )
    }

    private fun requireBackup(condition: Boolean, issue: BackupValidationIssue) {
        if (!condition) throw BackupValidationException(issue)
    }

    private val INACTIVE_VISIBILITY_VALUES = setOf(
        CalendarInactiveVisibilityDefaults.ACTIVE,
        CalendarInactiveVisibilityDefaults.ALL,
        CalendarInactiveVisibilityDefaults.INACTIVE,
    )
}
