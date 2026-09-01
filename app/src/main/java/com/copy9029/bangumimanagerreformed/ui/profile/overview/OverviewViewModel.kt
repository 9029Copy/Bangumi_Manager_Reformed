package com.copy9029.bangumimanagerreformed.ui.profile.overview

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject

data class OverviewSeason(
    val year: Int,
    val month: Int,
) {
    fun previous(): OverviewSeason {
        return if (month == 1) {
            OverviewSeason(year = year - 1, month = 10)
        } else {
            copy(month = month - 3)
        }
    }

    fun next(): OverviewSeason {
        return if (month == 10) {
            OverviewSeason(year = year + 1, month = 1)
        } else {
            copy(month = month + 3)
        }
    }
}

data class OverviewItemUiState(
    val bangumiId: Int,
    val title: String,
    val scoreTimesTen: Int?,
)

data class OverviewUiState(
    val selectedSeason: OverviewSeason = currentOverviewSeason(),
    val items: List<OverviewItemUiState> = emptyList(),
) {
    val previousSeason: OverviewSeason
        get() = selectedSeason.previous()

    val nextSeason: OverviewSeason
        get() = selectedSeason.next()
}

@HiltViewModel
class OverviewViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    fun onPreviousSeasonClick() {
        // TODO: 切换到上一季度，并加载该季度的项目概览。
    }

    fun onNextSeasonClick() {
        // TODO: 切换到下一季度，并加载该季度的项目概览。
    }

    fun onSeasonClick() {
        // TODO: 显示季度选择窗口，并在用户确认后加载目标季度。
    }
}

private fun currentOverviewSeason(today: LocalDate = LocalDate.now()): OverviewSeason {
    val seasonMonth = when (today.monthValue) {
        in 1..3 -> 1
        in 4..6 -> 4
        in 7..9 -> 7
        else -> 10
    }
    return OverviewSeason(
        year = today.year,
        month = seasonMonth,
    )
}
