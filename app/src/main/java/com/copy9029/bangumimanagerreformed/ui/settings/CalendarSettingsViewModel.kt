package com.copy9029.bangumimanagerreformed.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.CalendarInactiveVisibilityDefaults
import com.copy9029.bangumimanagerreformed.data.CalendarSettings
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<CalendarSettings> = settingsRepository.calendarSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarSettings(),
    )

    fun setInactiveVisibility(value: Int) {
        if (value !in INACTIVE_VISIBILITY_VALUES) return
        viewModelScope.launch {
            settingsRepository.setCalendarInactiveVisibility(value)
        }
    }

    fun setFinishedEpisodeVisible(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCalendarFinishedEpisodeVisible(value)
        }
    }

    fun setFinishedBangumiVisible(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCalendarFinishedBangumiVisible(value)
        }
    }

    fun setWeeksBeforeCurrent(value: Int) {
        viewModelScope.launch {
            settingsRepository.setCalendarWeeksBeforeCurrent(value.coerceIn(MIN_WEEK_COUNT, MAX_WEEK_COUNT))
        }
    }

    fun setWeeksAfterCurrent(value: Int) {
        viewModelScope.launch {
            settingsRepository.setCalendarWeeksAfterCurrent(value.coerceIn(MIN_WEEK_COUNT, MAX_WEEK_COUNT))
        }
    }

    fun setWeeksPrefix(value: Int) {
        viewModelScope.launch {
            settingsRepository.setCalendarWeeksPrefix(value.coerceIn(0, 10))
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetCalendarSettings()
        }
    }

    private companion object {
        const val MIN_WEEK_COUNT = 52
        const val MAX_WEEK_COUNT = 52 * 50

        val INACTIVE_VISIBILITY_VALUES = setOf(
            CalendarInactiveVisibilityDefaults.ACTIVE,
            CalendarInactiveVisibilityDefaults.ALL,
            CalendarInactiveVisibilityDefaults.INACTIVE,
        )
    }
}
