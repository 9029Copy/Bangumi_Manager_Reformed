package com.copy9029.bangumimanagerreformed.ui.bangumi.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.BangumiAddInfo
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.GlobalSettings
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.util.calcNearestSeason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BangumiAddSheetUiState(
    val seasonYear: Int,
    val seasonMonth: Int,
    val title: String = "",
    val firstBroadcastDate: LocalDate,
    val startYear: Int,
    val endYear: Int,
    val titleError: Int? = null,
    val isSubmitting: Boolean = false,
    val themeColorLong: Long = 0xFFFFFFFFL,
) {
    fun toAddInfo(): BangumiAddInfo {
        return BangumiAddInfo(
            seasonYear = seasonYear,
            seasonMonth = seasonMonth,
            title = title.trim(),
            firstBroadcastDate = firstBroadcastDate,
        )
    }
}



@HiltViewModel
class BangumiAddSheetViewModel @Inject constructor(
    private val repository: BangumiRepository,
    private val settingsRepository: SettingsRepository,
): ViewModel() {
    private var globalSettings = GlobalSettings()
    private val _uiState = MutableStateFlow<BangumiAddSheetUiState?>(null)
    val uiState: StateFlow<BangumiAddSheetUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.globalSettings.collect { settings ->
                globalSettings = settings
                _uiState.update { state ->
                    state?.copy(
                        themeColorLong = settings.colorForSeasonMonth(state.seasonMonth),
                    )
                }
            }
        }
    }

    fun initializeForOpen(defaultFirstBroadcastDate: LocalDate) {
        val existingState = _uiState.value
        if (existingState != null) {
            _uiState.value = existingState.copy(
                firstBroadcastDate = defaultFirstBroadcastDate,
            )
            return
        }

        val defaultSeason = calcNearestSeason(LocalDate.now())

        _uiState.value = BangumiAddSheetUiState(
            seasonYear = defaultSeason.year,
            seasonMonth = defaultSeason.monthValue,
            firstBroadcastDate = defaultFirstBroadcastDate,
            startYear = defaultSeason.year - 5,
            endYear = defaultSeason.year + 2,
            themeColorLong = globalSettings.colorForSeasonMonth(defaultSeason.monthValue),
        )
    }

    fun onSeasonChanged(year: Int, month: Int) {
        _uiState.update { state ->
            state?.copy(
                seasonYear = year,
                seasonMonth = month,
                themeColorLong = globalSettings.colorForSeasonMonth(month),
            )
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update { state ->
            state?.copy(
                title = value,
                titleError = null,
            )
        }
    }

    fun onFirstBroadcastDateChanged(date: LocalDate) {
        _uiState.update { state ->
            state?.copy(
                firstBroadcastDate = date,
            )
        }
    }

    suspend fun onConfirmClick(defaultFirstBroadcastDate: LocalDate): Boolean {
        if (_uiState.value?.isSubmitting == true) {
            return false
        }
        if (!validateBeforeSubmit()) {
            return false
        }

        val addInfo = requireNotNull(_uiState.value).toAddInfo()
        _uiState.update { state ->
            state?.copy(isSubmitting = true)
        }

        return try {
            repository.addNewBangumi(addInfo)

            _uiState.update { state ->
                state?.copy(
                    title = "",
                    firstBroadcastDate = defaultFirstBroadcastDate,
                    titleError = null,
                    isSubmitting = false,
                )
            }
            true
        } catch (exception: CancellationException) {
            _uiState.update { state ->
                state?.copy(isSubmitting = false)
            }
            throw exception
        } catch (_: Exception) {
            _uiState.update { state ->
                state?.copy(isSubmitting = false)
            }
            false
        }
    }

    private fun validateBeforeSubmit(): Boolean {
        val state = _uiState.value ?: return false

        if (state.title.isBlank()) {
            _uiState.value = state.copy(titleError = R.string.bangumi_error_title_required)
            return false
        }

        return true
    }

}
