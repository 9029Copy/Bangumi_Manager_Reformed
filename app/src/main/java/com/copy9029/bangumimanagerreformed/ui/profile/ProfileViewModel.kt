package com.copy9029.bangumimanagerreformed.ui.profile

import androidx.lifecycle.ViewModel
import com.copy9029.bangumimanagerreformed.data.AppThemeMode
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ProfileUiState(
    val appThemeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    val default01ColorLong: Long = SettingsRepository.DEFAULT_01_COLOR_LONG,
    val default04ColorLong: Long = SettingsRepository.DEFAULT_04_COLOR_LONG,
    val default07ColorLong: Long = SettingsRepository.DEFAULT_07_COLOR_LONG,
    val default10ColorLong: Long = SettingsRepository.DEFAULT_10_COLOR_LONG,
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onTopActionClick() {
        // TODO: Define the top app bar action.
    }

    fun onThemeModeClick() {
        // TODO: Open the theme mode selector.
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
}
