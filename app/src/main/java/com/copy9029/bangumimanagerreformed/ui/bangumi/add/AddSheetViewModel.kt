package com.copy9029.bangumimanagerreformed.ui.bangumi.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.BangumiAddInfo
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.themeColorByMonth
import com.copy9029.bangumimanagerreformed.util.calcNearestSeason
import com.copy9029.bangumimanagerreformed.util.parseSingleFormattedText
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val formattedText: String = "",
    val title: String = "",
    val firstBroadcastDate: LocalDate,
    val startYear: Int,
    val endYear: Int,
    val titleError: String? = null,
    val formattedTextError: String? = null,
) {
    val themeColorLong: Long
        get() = themeColorByMonth[seasonMonth] ?: 0xFFFFFFFFL

    fun toAddInfo(): BangumiAddInfo {
        return BangumiAddInfo(
            seasonYear = seasonYear,
            seasonMonth = seasonMonth,
            themeColorLong = themeColorLong,
            title = title.trim(),
            firstBroadcastDate = firstBroadcastDate,
        )
    }
}



@HiltViewModel
class AddSheetViewModel @Inject constructor(
    private val repository: BangumiRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow<BangumiAddSheetUiState?>(null)
    val uiState: StateFlow<BangumiAddSheetUiState?> = _uiState.asStateFlow()

    fun initializeIfNeeded(defaultFirstBroadcastDate: LocalDate) {
        if (_uiState.value != null) return

        val defaultSeason = calcNearestSeason(LocalDate.now())

        _uiState.value = BangumiAddSheetUiState(
            seasonYear = defaultSeason.year,
            seasonMonth = defaultSeason.monthValue,
            firstBroadcastDate = defaultFirstBroadcastDate,
            startYear = defaultSeason.year - 5,
            endYear = defaultSeason.year + 2,
        )
    }

    fun onSeasonChanged(year: Int, month: Int) {
        _uiState.update { state ->
            state?.copy(
                seasonYear = year,
                seasonMonth = month,
            )
        }
    }

    fun onFormattedTextChanged(value: String) {
        _uiState.update { state ->
            state?.copy(
                formattedText = value,
                formattedTextError = null,
            )
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update { state ->
            state?.copy(
                title = value,
                titleError = null,
                formattedTextError = null,
            )
        }
    }

    fun onFirstBroadcastDateChanged(date: LocalDate) {
        _uiState.update { state ->
            state?.copy(
                firstBroadcastDate = date,
                formattedTextError = null,
            )
        }
    }

    fun onParseFormattedText() {
        val state = _uiState.value ?: return
        val parsed = parseSingleFormattedText(state.formattedText)

        if (parsed == null) {
            _uiState.update {
                it?.copy(formattedTextError = "解析失败")
            }
            return
        }

        _uiState.update {
            it?.copy(
                title = parsed.title,
                firstBroadcastDate = parsed.firstBroadcastDate,
                titleError = null,
                formattedTextError = null,
            )
        }
    }

    fun onConfirmClick(defaultFirstBroadcastDate: LocalDate): Boolean {
        if (!validateBeforeSubmit()) {
            return false
        }

        val addInfo = requireNotNull(_uiState.value).toAddInfo()

        viewModelScope.launch {
            repository.addNewBangumi(addInfo)

            _uiState.update { state ->
                state?.copy(
                    formattedText = "",
                    title = "",
                    firstBroadcastDate = defaultFirstBroadcastDate,
                    titleError = null,
                    formattedTextError = null,
                )
            }
        }

        return true
    }

    private fun validateBeforeSubmit(): Boolean {
        val state = _uiState.value ?: return false

        if (state.title.isBlank()) {
            _uiState.value = state.copy(titleError = "标题不能为空")
            return false
        }

        return true
    }

}
