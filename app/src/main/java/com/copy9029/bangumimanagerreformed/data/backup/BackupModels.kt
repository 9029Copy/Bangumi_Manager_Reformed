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

internal class BackupValidationException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)

internal class BackupFileTooLargeException(
    val maxSizeMiB: Int,
) : IllegalArgumentException()
