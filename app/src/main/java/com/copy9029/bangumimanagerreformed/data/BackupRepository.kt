package com.copy9029.bangumimanagerreformed.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,   // FIXME
    private val database: AppDatabase,
    private val bangumiDao: BangumiDao,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun exportBackup(destination: Uri) = withContext(Dispatchers.IO) {
        val roomSnapshot = database.withTransaction {
            RoomBackupSnapshot(
                bangumis = bangumiDao.getAllBangumisOnce(),
                schedules = bangumiDao.getAllSchedulesOnce(),
            )
        }
        val calendarSettings = settingsRepository.calendarSettings.first()
        val globalSettings = settingsRepository.globalSettings.first()

        val backupJson = createBackupJson(
            roomSnapshot = roomSnapshot,
            calendarSettings = calendarSettings,
            globalSettings = globalSettings,
        )
        val outputStream = requireNotNull(
            context.contentResolver.openOutputStream(destination, "rwt"),
        ) { "无法打开所选备份文件" }

        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(backupJson.toString(JSON_INDENT_SPACES))
        }
    }

    private fun createBackupJson(
        roomSnapshot: RoomBackupSnapshot,
        calendarSettings: CalendarSettings,
        globalSettings: GlobalSettings,
    ): JSONObject = JSONObject().apply {
        put("formatVersion", BACKUP_FORMAT_VERSION)
        put("exportedAt", Instant.now().toString())
        put("appVersion", getAppVersionName())
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
        put("lastBasicInfoModifiedAtMillis", lastBasicInfoModifiedAtMillis)
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

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    @Suppress("DEPRECATION")
    private fun getAppVersionName(): String {
        return context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            ?: "unknown"
    }

    private data class RoomBackupSnapshot(
        val bangumis: List<Bangumi>,
        val schedules: List<BangumiSchedule>,
    )

    private companion object {
        const val BACKUP_FORMAT_VERSION = 1
        const val JSON_INDENT_SPACES = 2
    }
}
