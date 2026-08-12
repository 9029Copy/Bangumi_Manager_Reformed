package com.copy9029.bangumimanagerreformed.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.AppThemeMode
import com.copy9029.bangumimanagerreformed.data.GlobalSettings
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val appThemeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    val default01ColorLong: Long = SettingsRepository.DEFAULT_01_COLOR_LONG,
    val default04ColorLong: Long = SettingsRepository.DEFAULT_04_COLOR_LONG,
    val default07ColorLong: Long = SettingsRepository.DEFAULT_07_COLOR_LONG,
    val default10ColorLong: Long = SettingsRepository.DEFAULT_10_COLOR_LONG,
    val isThemeModeDialogVisible: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val isThemeModeDialogVisible = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        settingsRepository.globalSettings,
        isThemeModeDialogVisible,
    ) { settings, dialogVisible ->
        settings.toProfileUiState(dialogVisible)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    fun onThemeModeClick() {
        isThemeModeDialogVisible.value = true
    }

    fun onThemeModeDialogDismiss() {
        isThemeModeDialogVisible.value = false
    }

    fun onThemeModeSelected(value: AppThemeMode) {
        isThemeModeDialogVisible.value = false
        viewModelScope.launch {
            settingsRepository.setAppThemeMode(value)
        }
    }

    fun onDefault01ColorClick() {
        // TODO: Open the January default color selector.
    }

    fun onDefault04ColorClick() {
        // TODO: Open the April default color selector.
    }

    fun onDefault07ColorClick() {
        // TODO: Open the July default color selector.
    }

    fun onDefault10ColorClick() {
        // TODO: Open the October default color selector.
    }

    fun onTopActionClick() {
        // TODO: Define the top app bar action.
    }
}

private fun GlobalSettings.toProfileUiState(
    isThemeModeDialogVisible: Boolean,
): ProfileUiState {
    return ProfileUiState(
        appThemeMode = appThemeMode,
        default01ColorLong = default01ColorLong,
        default04ColorLong = default04ColorLong,
        default07ColorLong = default07ColorLong,
        default10ColorLong = default10ColorLong,
        isThemeModeDialogVisible = isThemeModeDialogVisible,
    )
}
