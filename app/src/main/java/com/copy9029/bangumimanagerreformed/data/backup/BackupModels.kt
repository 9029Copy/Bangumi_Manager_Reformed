package com.copy9029.bangumimanagerreformed.data.backup

import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.CalendarSettings
import com.copy9029.bangumimanagerreformed.data.GlobalSettings

data class BackupImportData(
    val bangumis: List<Bangumi>,
    val schedules: List<BangumiSchedule>,
    val calendarSettings: CalendarSettings,
    val globalSettings: GlobalSettings,
    val settingItemCount: Int,
)

data class BackupExportResult(
    val fileSizeBytes: Int,
    val exceedsImportSizeLimit: Boolean,
)

internal data class RoomBackupSnapshot(
    val bangumis: List<Bangumi>,
    val schedules: List<BangumiSchedule>,
)

internal object BackupFormat {
    const val MIN_SUPPORTED_VERSION = 1
    const val CURRENT_VERSION = 2
    const val JSON_INDENT_SPACES = 2
    const val MAX_FILE_SIZE_MIB = 10
    const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MIB * 1024 * 1024
    const val MIN_SEASON_YEAR = 1
    const val MAX_SEASON_YEAR = 9999
    const val MIN_CALENDAR_WEEKS = 52
    const val MAX_CALENDAR_WEEKS = 52 * 50
    const val MAX_CALENDAR_WEEKS_PREFIX = 10
    const val MIN_ARGB_COLOR_LONG = 0L
    const val MAX_ARGB_COLOR_LONG = 0xFFFFFFFFL

    val SEASON_MONTHS = setOf(1, 4, 7, 10)

    fun isSupported(version: Int): Boolean {
        return version in MIN_SUPPORTED_VERSION..CURRENT_VERSION
    }
}

sealed interface BackupSection {
    data object Root : BackupSection
    data class BangumiItem(val index: Int) : BackupSection
    data class ScheduleItem(val index: Int) : BackupSection
    data object CalendarSettings : BackupSection
    data object GlobalSettings : BackupSection
}

enum class BangumiValidationReason {
    ID_NON_POSITIVE,
    TITLE_BLANK,
    SEASON_YEAR_OUT_OF_RANGE,
    SEASON_MONTH_INVALID,
    SCORE_OUT_OF_RANGE,
    TOTAL_EPISODES_NON_POSITIVE,
    WATCHED_EPISODE_NEGATIVE,
    WATCHED_EPISODE_EXCEEDS_TOTAL,
    LAST_MODIFIED_NEGATIVE,
}

enum class ScheduleValidationReason {
    MISSING_BANGUMI_REFERENCE,
    EPISODE_NON_POSITIVE,
    DUPLICATE,
    EPISODE_EXCEEDS_TOTAL,
}

enum class CalendarSettingValidationReason {
    INACTIVE_VISIBILITY_INVALID,
    WEEKS_BEFORE_OUT_OF_RANGE,
    WEEKS_AFTER_OUT_OF_RANGE,
    WEEKS_PREFIX_OUT_OF_RANGE,
}

sealed interface BackupValidationIssue {
    data class MissingField(
        val section: BackupSection,
        val fieldName: String,
    ) : BackupValidationIssue

    data class UnsupportedFormatVersion(val version: Int) : BackupValidationIssue
    data object MalformedJson : BackupValidationIssue
    data class InvalidDateOrTime(val value: String?) : BackupValidationIssue
    data object InvalidContent : BackupValidationIssue
    data object InvalidExportedAt : BackupValidationIssue
    data object BlankAppVersion : BackupValidationIssue
    data object DuplicateBangumiId : BackupValidationIssue
    data class InvalidBangumi(
        val bangumiId: Int,
        val reason: BangumiValidationReason,
    ) : BackupValidationIssue
    data class InvalidSchedule(
        val bangumiId: Int,
        val episodeId: Int,
        val reason: ScheduleValidationReason,
    ) : BackupValidationIssue
    data class MissingFirstSchedule(val bangumiId: Int) : BackupValidationIssue
    data class FirstScheduleDateMismatch(val bangumiId: Int) : BackupValidationIssue
    data class InvalidCalendarSetting(
        val reason: CalendarSettingValidationReason,
    ) : BackupValidationIssue
    data class InvalidColor(val month: Int) : BackupValidationIssue
    data class UnknownThemeMode(val storedValue: String) : BackupValidationIssue
    data class ExpectedEndDateOutOfRange(val bangumiId: Int) : BackupValidationIssue
}

internal class BackupValidationException(
    val issue: BackupValidationIssue,
    cause: Throwable? = null,
) : IllegalArgumentException(issue.toString(), cause)

internal class BackupFileTooLargeException(
    val maxSizeMiB: Int,
) : IllegalArgumentException()
