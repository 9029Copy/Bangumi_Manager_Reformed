package com.copy9029.bangumimanagerreformed.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.backup.BackupImportData
import com.copy9029.bangumimanagerreformed.data.backup.BackupFileTooLargeException
import com.copy9029.bangumimanagerreformed.data.backup.BackupRepository
import com.copy9029.bangumimanagerreformed.data.backup.BackupValidationException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val importConfirmation: BackupImportConfirmation? = null,
)

data class BackupImportConfirmation(
    val bangumiCount: Int,
    val scheduleCount: Int,
    val settingCount: Int,
)

sealed interface BackupMessage {
    data class ExportSuccess(
        val fileSizeText: String,
        val exceedsImportSizeLimit: Boolean,
    ) : BackupMessage

    data object ExportFailure : BackupMessage
    data class ReadTooLarge(val maxSizeMiB: Int) : BackupMessage
    data object ReadInvalid : BackupMessage
    data object ReadFailure : BackupMessage
    data object ImportSuccess : BackupMessage
    data object ImportFailure : BackupMessage
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<BackupMessage>()
    val messages: SharedFlow<BackupMessage> = _messages.asSharedFlow()

    private var pendingImport: BackupImportData? = null

    fun onExportDestinationSelected(uri: Uri) {
        if (!tryStartOperation(BackupOperation.EXPORT)) return

        viewModelScope.launch {
            try {
                val result = backupRepository.exportBackup(uri)
                val sizeText = result.fileSizeBytes.toReadableFileSize()
                _messages.emit(
                    BackupMessage.ExportSuccess(
                        fileSizeText = sizeText,
                        exceedsImportSizeLimit = result.exceedsImportSizeLimit,
                    )
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _messages.emit(BackupMessage.ExportFailure)
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    fun onImportFileSelected(uri: Uri) {
        if (!tryStartOperation(BackupOperation.IMPORT)) return

        viewModelScope.launch {
            try {
                val backup = backupRepository.readBackup(uri)
                pendingImport = backup
                _uiState.update {
                    it.copy(
                        importConfirmation = BackupImportConfirmation(
                            bangumiCount = backup.bangumis.size,
                            scheduleCount = backup.schedules.size,
                            settingCount = backup.settingItemCount,
                        ),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: BackupFileTooLargeException) {
                pendingImport = null
                _messages.emit(BackupMessage.ReadTooLarge(exception.maxSizeMiB))
            } catch (_: BackupValidationException) {
                pendingImport = null
                _messages.emit(BackupMessage.ReadInvalid)
            } catch (_: Exception) {
                pendingImport = null
                _messages.emit(BackupMessage.ReadFailure)
            } finally {
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

    fun onImportDismiss() {
        if (_uiState.value.isImporting) return

        pendingImport = null
        _uiState.update { it.copy(importConfirmation = null) }
    }

    fun onImportConfirm() {
        val backup = pendingImport ?: return
        if (!tryStartOperation(BackupOperation.IMPORT)) return

        viewModelScope.launch {
            try {
                backupRepository.replaceAllData(backup)
                pendingImport = null
                _uiState.update { it.copy(importConfirmation = null) }
                _messages.emit(BackupMessage.ImportSuccess)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _messages.emit(BackupMessage.ImportFailure)
            } finally {
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

    private fun tryStartOperation(operation: BackupOperation): Boolean {
        while (true) {
            val currentState = _uiState.value
            if (currentState.isExporting || currentState.isImporting) return false

            val busyState = when (operation) {
                BackupOperation.EXPORT -> currentState.copy(isExporting = true)
                BackupOperation.IMPORT -> currentState.copy(isImporting = true)
            }
            if (_uiState.compareAndSet(currentState, busyState)) return true
        }
    }

    private enum class BackupOperation {
        EXPORT,
        IMPORT,
    }
}

private fun Int.toReadableFileSize(): String {
    return when {
        this >= BYTES_PER_MIB -> String.format(
            Locale.getDefault(),
            "%.2f MiB",
            this.toDouble() / BYTES_PER_MIB,
        )
        this >= BYTES_PER_KIB -> String.format(
            Locale.getDefault(),
            "%.2f KiB",
            this.toDouble() / BYTES_PER_KIB,
        )
        else -> "$this B"
    }
}

private const val BYTES_PER_KIB = 1024
private const val BYTES_PER_MIB = 1024 * 1024
