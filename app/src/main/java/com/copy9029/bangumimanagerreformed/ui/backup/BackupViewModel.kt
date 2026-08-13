package com.copy9029.bangumimanagerreformed.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.BackupRepository
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
import javax.inject.Inject

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun onExportDestinationSelected(uri: Uri) {
        if (_uiState.value.isExporting || _uiState.value.isImporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                backupRepository.exportBackup(uri)
                _messages.emit("备份导出成功")
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _messages.emit("备份导出失败：${exception.message ?: "未知错误"}")
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    fun onImportFileSelected(uri: Uri) {
        if (_uiState.value.isExporting || _uiState.value.isImporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                // TODO: Read and parse the backup JSON, validate it, then request confirmation.
                @Suppress("UNUSED_VARIABLE")
                val selectedBackup = uri
            } finally {
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }
}
