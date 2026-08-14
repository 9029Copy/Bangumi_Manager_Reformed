package com.copy9029.bangumimanagerreformed.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.copy9029.bangumimanagerreformed.util.calculateExpectedEndDate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

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

@Singleton
class BackupRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val bangumiDao: BangumiDao,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun exportBackup(destination: Uri): BackupExportResult = withContext(Dispatchers.IO) {
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
        val backupBytes = backupJson
            .toString(JSON_INDENT_SPACES)
            .toByteArray(Charsets.UTF_8)
        val outputStream = requireNotNull(
            context.contentResolver.openOutputStream(destination, "rwt"),
        ) { "无法打开所选备份文件" }

        outputStream.buffered().use { stream ->
            stream.write(backupBytes)
        }

        BackupExportResult(
            fileSizeBytes = backupBytes.size,
            exceedsImportSizeLimit = backupBytes.size > MAX_BACKUP_FILE_SIZE_BYTES,
        )
    }

    suspend fun readBackup(source: Uri): BackupImportData = withContext(Dispatchers.IO) {
        val inputStream = requireNotNull(
            context.contentResolver.openInputStream(source),
        ) { "无法打开所选备份文件" }
        val jsonText = inputStream.use { stream ->
            stream.readBytesWithLimit(MAX_BACKUP_FILE_SIZE_BYTES).toString(Charsets.UTF_8)
        }
        try {
            val root = JSONObject(jsonText)
            validateBackupStructure(root)
            val backup = root.toBackupImportData()

            validateBackupJson(root, backup)
            backup.withRecalculatedExpectedEndDates()
        } catch (exception: BackupValidationException) {
            throw exception
        } catch (exception: JSONException) {
            throw BackupValidationException(
                message = "JSON 结构错误、字段缺失或字段类型不正确：${exception.message}",
                cause = exception,
            )
        } catch (exception: DateTimeParseException) {
            throw BackupValidationException(
                message = "日期或时间格式不正确：${exception.parsedString}",
                cause = exception,
            )
        } catch (exception: IllegalArgumentException) {
            throw BackupValidationException(
                message = exception.message ?: "备份内容不合法",
                cause = exception,
            )
        }
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

    private fun JSONObject.toGlobalSettings(): GlobalSettings = GlobalSettings(
        appThemeMode = requireNotNull(
            AppThemeMode.fromStoredValue(getString("appThemeMode")),
        ) { "备份中包含未知的主题模式" },
        default01ColorLong = getLong("default01ColorLong"),
        default04ColorLong = getLong("default04ColorLong"),
        default07ColorLong = getLong("default07ColorLong"),
        default10ColorLong = getLong("default10ColorLong"),
    )

    private fun validateBackupStructure(root: JSONObject) {
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

        val bangumis = root.getJSONArray("bangumis")
        repeat(bangumis.length()) { index ->
            bangumis.getJSONObject(index).requireKeys(
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
                "lastBasicInfoModifiedAtMillis",
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

    private fun validateBackupJson(root: JSONObject, backup: BackupImportData) {
        requireBackup(root.getInt("formatVersion") == BACKUP_FORMAT_VERSION) {
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
            requireBackup(bangumi.seasonYear in MIN_SEASON_YEAR..MAX_SEASON_YEAR) {
                "$itemName 的季度年份超出范围"
            }
            requireBackup(bangumi.seasonMonth in SEASON_MONTHS) {
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
            ) {
                "$itemName 的已观看集数不能大于总集数"
            }
            requireBackup(bangumi.lastBasicInfoModifiedAtMillis >= 0L) {
                "$itemName 的最近编辑时间不能小于 0"
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
            calendarSettings.calendarWeeksBeforeCurrent in MIN_CALENDAR_WEEKS..MAX_CALENDAR_WEEKS,
        ) { "日历向前显示的周数超出范围" }
        requireBackup(
            calendarSettings.calendarWeeksAfterCurrent in MIN_CALENDAR_WEEKS..MAX_CALENDAR_WEEKS,
        ) { "日历向后显示的周数超出范围" }
        requireBackup(calendarSettings.calendarWeeksPrefix in 0..MAX_CALENDAR_WEEKS_PREFIX) {
            "日历周数前缀超出范围"
        }

        with(backup.globalSettings) {
            validateColorLong("1 月默认颜色", default01ColorLong)
            validateColorLong("4 月默认颜色", default04ColorLong)
            validateColorLong("7 月默认颜色", default07ColorLong)
            validateColorLong("10 月默认颜色", default10ColorLong)
        }
    }

    private fun BackupImportData.withRecalculatedExpectedEndDates(): BackupImportData {
        val schedulesByBangumiId = schedules.groupBy(BangumiSchedule::bangumiId)
        return copy(
            bangumis = bangumis.map { bangumi ->
                bangumi.copy(
                    expectedEndDate = bangumi.calculateExpectedEndDate(
                        schedulesByBangumiId[bangumi.bangumiId].orEmpty(),
                    ),
                )
            },
        )
    }

    private fun JSONObject.requireKeys(sectionName: String, vararg keys: String) {
        keys.forEach { key ->
            requireBackup(has(key)) { "$sectionName 缺少必要字段：$key" }
        }
    }

    private fun validateColorLong(name: String, value: Long) {
        requireBackup(value in MIN_ARGB_COLOR_LONG..MAX_ARGB_COLOR_LONG) {
            "$name 不是有效的 ARGB 色值"
        }
    }

    private inline fun requireBackup(condition: Boolean, message: () -> String) {
        if (!condition) throw BackupValidationException(message())
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

    private fun InputStream.readBytesWithLimit(maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytes = 0

        while (true) {
            val bytesRead = read(buffer)
            if (bytesRead < 0) break
            if (totalBytes + bytesRead > maxBytes) {
                throw IllegalArgumentException(
                    "备份文件不能超过 $MAX_BACKUP_FILE_SIZE_MIB MiB",
                )
            }
            output.write(buffer, 0, bytesRead)
            totalBytes += bytesRead
        }

        return output.toByteArray()
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

    private class BackupValidationException(
        message: String,
        cause: Throwable? = null,
    ) : IllegalArgumentException(message, cause)

    private companion object {
        const val BACKUP_FORMAT_VERSION = 1
        const val JSON_INDENT_SPACES = 2
        const val MAX_BACKUP_FILE_SIZE_MIB = 10
        const val MAX_BACKUP_FILE_SIZE_BYTES = MAX_BACKUP_FILE_SIZE_MIB * 1024 * 1024
        const val MIN_SEASON_YEAR = 1
        const val MAX_SEASON_YEAR = 9999
        const val MIN_CALENDAR_WEEKS = 52
        const val MAX_CALENDAR_WEEKS = 52 * 50
        const val MAX_CALENDAR_WEEKS_PREFIX = 10
        const val MIN_ARGB_COLOR_LONG = 0L
        const val MAX_ARGB_COLOR_LONG = 0xFFFFFFFFL

        val SEASON_MONTHS = setOf(1, 4, 7, 10)
        val INACTIVE_VISIBILITY_VALUES = setOf(
            CalendarInactiveVisibilityDefaults.ACTIVE,
            CalendarInactiveVisibilityDefaults.ALL,
            CalendarInactiveVisibilityDefaults.INACTIVE,
        )
    }
}
