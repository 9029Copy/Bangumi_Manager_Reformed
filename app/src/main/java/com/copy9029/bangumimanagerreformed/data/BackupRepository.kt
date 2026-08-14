package com.copy9029.bangumimanagerreformed.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class BackupImportData(
    val bangumis: List<Bangumi>,
    val schedules: List<BangumiSchedule>,
    val calendarSettings: CalendarSettings,
    val globalSettings: GlobalSettings,
    val settingItemCount: Int,
)

@Singleton
class BackupRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
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

    suspend fun readBackup(source: Uri): BackupImportData = withContext(Dispatchers.IO) {
        val inputStream = requireNotNull(
            context.contentResolver.openInputStream(source),
        ) { "无法打开所选备份文件" }
        val jsonText = inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
        val root = JSONObject(jsonText)
        val backup = root.toBackupImportData()

        validateBackupJson(root, backup)
        backup
    }

    suspend fun replaceAllData(backup: BackupImportData) = withContext(Dispatchers.IO) {
        val previousRoomSnapshot = database.withTransaction {
            RoomBackupSnapshot(
                bangumis = bangumiDao.getAllBangumisOnce(),
                schedules = bangumiDao.getAllSchedulesOnce(),
            )
        }
        val previousCalendarSettings = settingsRepository.calendarSettings.first()
        val previousGlobalSettings = settingsRepository.globalSettings.first()

        // Room 和 DataStore 无法共享同一个事务。进入写入阶段后不响应协程取消，
        // 并在设置写入失败时尽力恢复 Room 和 DataStore 的原数据。
        withContext(NonCancellable) {
            replaceRoomData(backup.bangumis, backup.schedules)

            try {
                settingsRepository.replaceAllSettings(
                    calendarSettings = backup.calendarSettings,
                    globalSettings = backup.globalSettings,
                )
            } catch (writeException: Exception) {
                runCatching {
                    replaceRoomData(
                        previousRoomSnapshot.bangumis,
                        previousRoomSnapshot.schedules,
                    )
                }.exceptionOrNull()?.let(writeException::addSuppressed)
                runCatching {
                    settingsRepository.replaceAllSettings(
                        calendarSettings = previousCalendarSettings,
                        globalSettings = previousGlobalSettings,
                    )
                }.exceptionOrNull()?.let(writeException::addSuppressed)
                throw writeException
            }
        }
    }

    private suspend fun replaceRoomData(
        bangumis: List<Bangumi>,
        schedules: List<BangumiSchedule>,
    ) {
        database.withTransaction {
            // 先删除子表，再删除父表；写入时则反过来，以满足外键约束。
            bangumiDao.deleteAllSchedules()
            bangumiDao.deleteAllBangumis()
            bangumiDao.insertBangumis(bangumis)
            bangumiDao.insertSchedules(schedules)
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
        lastBasicInfoModifiedAtMillis = getLong("lastBasicInfoModifiedAtMillis"),
        expectedEndDate = getNullableString("expectedEndDate")?.let(LocalDate::parse),
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

    private fun JSONObject.toGlobalSettings(): GlobalSettings = GlobalSettings(
        appThemeMode = requireNotNull(
            AppThemeMode.fromStoredValue(getString("appThemeMode")),
        ) { "备份中包含未知的主题模式" },
        default01ColorLong = getLong("default01ColorLong"),
        default04ColorLong = getLong("default04ColorLong"),
        default07ColorLong = getLong("default07ColorLong"),
        default10ColorLong = getLong("default10ColorLong"),
    )

    private fun validateBackupJson(root: JSONObject, backup: BackupImportData) {
        @Suppress("UNUSED_VARIABLE")
        val parsedBackup = backup
        @Suppress("UNUSED_VARIABLE")
        val sourceJson = root

        // TODO: 校验格式版本、字段范围、ID 唯一性、Schedule 引用及首集日期等内容。
    }

    private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
        return List(length()) { index -> transform(getJSONObject(index)) }
    }

    private fun JSONObject.getNullableInt(key: String): Int? {
        return if (isNull(key)) null else getInt(key)
    }

    private fun JSONObject.getNullableString(key: String): String? {
        return if (isNull(key)) null else getString(key)
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
