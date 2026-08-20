package com.copy9029.bangumimanagerreformed.data.backup

import com.copy9029.bangumimanagerreformed.data.AppThemeMode
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.CalendarSettings
import com.copy9029.bangumimanagerreformed.data.GlobalSettings
import com.copy9029.bangumimanagerreformed.util.calculateExpectedEndDate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

internal class BackupJsonCodec {
    fun encode(
        roomSnapshot: RoomBackupSnapshot,
        calendarSettings: CalendarSettings,
        globalSettings: GlobalSettings,
        appVersion: String,
    ): ByteArray {
        return createBackupJson(
            roomSnapshot = roomSnapshot,
            calendarSettings = calendarSettings,
            globalSettings = globalSettings,
            appVersion = appVersion,
        ).toString(BackupFormat.JSON_INDENT_SPACES)
            .toByteArray(Charsets.UTF_8)
    }

    fun decode(jsonText: String): BackupImportData {
        try {
            val root = JSONObject(jsonText)
            BackupValidator.validateStructure(root)
            val backup = root.toBackupImportData()

            BackupValidator.validateContent(root, backup)
            return backup.withRecalculatedExpectedEndDates()
        } catch (exception: BackupValidationException) {
            throw exception
        } catch (exception: JSONException) {
            throw BackupValidationException(
                issue = BackupValidationIssue.MalformedJson,
                cause = exception,
            )
        } catch (exception: DateTimeParseException) {
            throw BackupValidationException(
                issue = BackupValidationIssue.InvalidDateOrTime(exception.parsedString),
                cause = exception,
            )
        } catch (exception: IllegalArgumentException) {
            throw BackupValidationException(
                issue = BackupValidationIssue.InvalidContent,
                cause = exception,
            )
        }
    }

    private fun createBackupJson(
        roomSnapshot: RoomBackupSnapshot,
        calendarSettings: CalendarSettings,
        globalSettings: GlobalSettings,
        appVersion: String,
    ): JSONObject = JSONObject().apply {
        put("formatVersion", BackupFormat.CURRENT_VERSION)
        put("exportedAt", Instant.now().toString())
        put("appVersion", appVersion)
        put(
            "bangumis",
            JSONArray().apply {
                roomSnapshot.bangumis.forEach { bangumi ->
                    put(bangumi.toBackupJson())
                }
            },
        )
        put(
            "schedules",
            JSONArray().apply {
                roomSnapshot.schedules.forEach { schedule ->
                    put(schedule.toBackupJson())
                }
            },
        )
        put("calendarSettings", calendarSettings.toBackupJson())
        put("globalSettings", globalSettings.toBackupJson())
    }

    private fun Bangumi.toBackupJson(): JSONObject = JSONObject().apply {
        put("bangumiId", bangumiId)
        put("title", title)
        put("seasonYear", seasonYear)
        put("seasonMonth", seasonMonth)
        putNullable("myScore", myScore)
        put("firstBroadcastDate", firstBroadcastDate.toString())
        putNullable("totalEpisodes", totalEpisodes)
        put("latestWatchedEpisode", latestWatchedEpisode)
        put("isActive", isActive)
        put("lastModifiedAtMillis", lastModifiedAtMillis)
        putNullable("expectedEndDate", expectedEndDate?.toString())
    }

    private fun BangumiSchedule.toBackupJson(): JSONObject = JSONObject().apply {
        put("bangumiId", bangumiId)
        put("episodeId", episodeId)
        put("broadcastDate", broadcastDate.toString())
    }

    private fun CalendarSettings.toBackupJson(): JSONObject = JSONObject().apply {
        put("calendarInactiveVisibility", calendarInactiveVisibility)
        put("calendarFinishedEpisodeVisible", calendarFinishedEpisodeVisible)
        put("calendarFinishedBangumiVisible", calendarFinishedBangumiVisible)
        put("calendarWeeksBeforeCurrent", calendarWeeksBeforeCurrent)
        put("calendarWeeksAfterCurrent", calendarWeeksAfterCurrent)
        put("calendarWeeksPrefix", calendarWeeksPrefix)
    }

    private fun GlobalSettings.toBackupJson(): JSONObject = JSONObject().apply {
        put("appThemeMode", appThemeMode.storedValue)
        put("default01ColorLong", default01ColorLong)
        put("default04ColorLong", default04ColorLong)
        put("default07ColorLong", default07ColorLong)
        put("default10ColorLong", default10ColorLong)
    }

    private fun JSONObject.toBackupImportData(): BackupImportData {
        val bangumiArray = getJSONArray("bangumis")
        val scheduleArray = getJSONArray("schedules")
        val calendarSettingsJson = getJSONObject("calendarSettings")
        val globalSettingsJson = getJSONObject("globalSettings")

        return BackupImportData(
            bangumis = bangumiArray.mapObjects { json -> json.toBangumi() },
            schedules = scheduleArray.mapObjects { json -> json.toBangumiSchedule() },
            calendarSettings = calendarSettingsJson.toCalendarSettings(),
            globalSettings = globalSettingsJson.toGlobalSettings(),
            settingItemCount = calendarSettingsJson.length() + globalSettingsJson.length(),
        )
    }

    private fun JSONObject.toBangumi(): Bangumi = Bangumi(
        bangumiId = getInt("bangumiId"),
        title = getString("title"),
        seasonYear = getInt("seasonYear"),
        seasonMonth = getInt("seasonMonth"),
        myScore = getNullableInt("myScore"),
        firstBroadcastDate = LocalDate.parse(getString("firstBroadcastDate")),
        totalEpisodes = getNullableInt("totalEpisodes"),
        latestWatchedEpisode = getInt("latestWatchedEpisode"),
        isActive = getBoolean("isActive"),
        lastModifiedAtMillis = when {
            has("lastModifiedAtMillis") -> getLong("lastModifiedAtMillis")
            else -> getLong("lastBasicInfoModifiedAtMillis")
        },
        expectedEndDate = null,
    )

    private fun JSONObject.toBangumiSchedule(): BangumiSchedule = BangumiSchedule(
        bangumiId = getInt("bangumiId"),
        episodeId = getInt("episodeId"),
        broadcastDate = LocalDate.parse(getString("broadcastDate")),
    )

    private fun JSONObject.toCalendarSettings(): CalendarSettings = CalendarSettings(
        calendarInactiveVisibility = getInt("calendarInactiveVisibility"),
        calendarFinishedEpisodeVisible = getBoolean("calendarFinishedEpisodeVisible"),
        calendarFinishedBangumiVisible = getBoolean("calendarFinishedBangumiVisible"),
        calendarWeeksBeforeCurrent = getInt("calendarWeeksBeforeCurrent"),
        calendarWeeksAfterCurrent = getInt("calendarWeeksAfterCurrent"),
        calendarWeeksPrefix = getInt("calendarWeeksPrefix"),
    )

    private fun JSONObject.toGlobalSettings(): GlobalSettings {
        val storedThemeMode = getString("appThemeMode")
        val appThemeMode = AppThemeMode.fromStoredValue(storedThemeMode)
            ?: throw BackupValidationException(
                BackupValidationIssue.UnknownThemeMode(storedThemeMode),
            )

        return GlobalSettings(
            appThemeMode = appThemeMode,
            default01ColorLong = getLong("default01ColorLong"),
            default04ColorLong = getLong("default04ColorLong"),
            default07ColorLong = getLong("default07ColorLong"),
            default10ColorLong = getLong("default10ColorLong"),
        )
    }

    private fun BackupImportData.withRecalculatedExpectedEndDates(): BackupImportData {
        val schedulesByBangumiId = schedules.groupBy(BangumiSchedule::bangumiId)
        return copy(
            bangumis = bangumis.map { bangumi ->
                val expectedEndDate = try {
                    bangumi.calculateExpectedEndDate(
                        schedulesByBangumiId[bangumi.bangumiId].orEmpty(),
                    )
                } catch (exception: DateTimeException) {
                    throw BackupValidationException(
                        issue = BackupValidationIssue.ExpectedEndDateOutOfRange(
                            bangumi.bangumiId,
                        ),
                        cause = exception,
                    )
                }
                bangumi.copy(
                    expectedEndDate = expectedEndDate,
                )
            },
        )
    }

    private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
        return List(length()) { index -> transform(getJSONObject(index)) }
    }

    private fun JSONObject.getNullableInt(key: String): Int? {
        return if (isNull(key)) null else getInt(key)
    }

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }
}
