package com.copy9029.bangumimanagerreformed.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

private const val WEEKS_BEFORE_CURRENT = 520
private const val WEEKS_AFTER_CURRENT = 520

data class CalendarBangumiItemUiState(
    val bangumiId: Int,
    val episodeId: Int,
    val title: String,
    val themeColorLong: Long,
)

data class CalendarUiState(
    val firstWeekStart: LocalDate = currentWeekStart()
        .minusWeeks(WEEKS_BEFORE_CURRENT.toLong()),
    val weekCount: Int = WEEKS_BEFORE_CURRENT + WEEKS_AFTER_CURRENT + 1,
    val initialWeekIndex: Int = WEEKS_BEFORE_CURRENT,
    val bangumisByDate: Map<LocalDate, List<CalendarBangumiItemUiState>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    repository: BangumiRepository,
): ViewModel() {

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.getAllBangumis(),
        repository.getAllSchedules(),
    ) { _, _ ->
        // TODO: 根据全部 Bangumi 与全部 Schedule 计算 CalendarUiState。
        CalendarUiState()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(isLoading = true),
    )
}

private fun currentWeekStart(today: LocalDate = LocalDate.now()): LocalDate {
    return today.minusDays((today.dayOfWeek.value - 1).toLong())
}
