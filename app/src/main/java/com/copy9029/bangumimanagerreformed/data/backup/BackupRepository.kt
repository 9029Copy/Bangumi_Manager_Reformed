package com.copy9029.bangumimanagerreformed.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.copy9029.bangumimanagerreformed.data.AppDatabase
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiDao
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val bangumiDao: BangumiDao,
    private val settingsRepository: SettingsRepository,
) {
    private val jsonCodec = BackupJsonCodec()

    suspend fun exportBackup(destination: Uri): BackupExportResult = withContext(Dispatchers.IO) {
        val roomSnapshot = database.withTransaction {
            RoomBackupSnapshot(
                bangumis = bangumiDao.getAllBangumisOnce(),
                schedules = bangumiDao.getAllSchedulesOnce(),
            )
        }
        val calendarSettings = settingsRepository.calendarSettings.first()
        val globalSettings = settingsRepository.globalSettings.first()
        val backupBytes = jsonCodec.encode(
            roomSnapshot = roomSnapshot,
            calendarSettings = calendarSettings,
            globalSettings = globalSettings,
            appVersion = getAppVersionName(),
        )
        val outputStream = requireNotNull(
            context.contentResolver.openOutputStream(destination, "rwt"),
        ) { "无法打开所选备份文件" }

        outputStream.buffered().use { stream ->
            stream.write(backupBytes)
        }

        BackupExportResult(
            fileSizeBytes = backupBytes.size,
            exceedsImportSizeLimit = backupBytes.size > BackupFormat.MAX_FILE_SIZE_BYTES,
        )
    }

    suspend fun readBackup(source: Uri): BackupImportData = withContext(Dispatchers.IO) {
        val inputStream = requireNotNull(
            context.contentResolver.openInputStream(source),
        ) { "无法打开所选备份文件" }
        val jsonText = inputStream.use { stream ->
            stream.readBytesWithLimit(BackupFormat.MAX_FILE_SIZE_BYTES)
                .toString(Charsets.UTF_8)
        }

        jsonCodec.decode(jsonText)
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

    private fun InputStream.readBytesWithLimit(maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytes = 0

        while (true) {
            val bytesRead = read(buffer)
            if (bytesRead < 0) break
            if (totalBytes + bytesRead > maxBytes) {
                throw BackupFileTooLargeException(BackupFormat.MAX_FILE_SIZE_MIB)
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
}
