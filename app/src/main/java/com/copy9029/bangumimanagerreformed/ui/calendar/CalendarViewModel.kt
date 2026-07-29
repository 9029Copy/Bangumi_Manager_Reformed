package com.copy9029.bangumimanagerreformed.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

private const val WEEKS_BEFORE_CURRENT = 520
private const val WEEKS_AFTER_CURRENT = 520
const val WEEKS_PREFIX = 1

data class CalendarBangumiItemUiState(
    val bangumiId: Int,
    val episodeId: Int,
    val title: String,
    val themeColorLong: Long,
    val isDone: Boolean,
)

data class CalendarUiState(
    val firstWeekStart: LocalDate = currentWeekStart()
        .minusWeeks(WEEKS_BEFORE_CURRENT.toLong()),
    val weekCount: Int = WEEKS_BEFORE_CURRENT + WEEKS_AFTER_CURRENT + 1,
    val initialWeekIndex: Int = WEEKS_BEFORE_CURRENT - WEEKS_PREFIX,
    val bangumisByDate: Map<LocalDate, List<CalendarBangumiItemUiState>> = emptyMap(),
    val selectedDateEpochDay: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    fun isDateSelected(date: LocalDate): Boolean {
        return selectedDateEpochDay == date.toEpochDay()
    }

    fun isWeekSelected(weekStart: LocalDate): Boolean {
        val selectedEpochDay = selectedDateEpochDay ?: return false
        val weekEndEpochDay = weekStart.plusDays(6).toEpochDay()
        return selectedEpochDay in weekStart.toEpochDay()..weekEndEpochDay
    }
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    repository: BangumiRepository,
): ViewModel() {

    private val _selectedDateEpochDay = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.getAllBangumis(),
        repository.getAllSchedules(),
        _selectedDateEpochDay,
    ) { _, _, selectedDateEpochDay ->
        // TODO: 根据全部 Bangumi 与全部 Schedule 计算 CalendarUiState。
        CalendarUiState(
            selectedDateEpochDay = selectedDateEpochDay,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(isLoading = true),
    )

    fun onDateClick(date: LocalDate) {
        val clickedDateEpochDay = date.toEpochDay()
        _selectedDateEpochDay.update { selectedDateEpochDay ->
            if (selectedDateEpochDay == clickedDateEpochDay) {
                null
            } else {
                clickedDateEpochDay
            }
        }
    }
}

private fun currentWeekStart(today: LocalDate = LocalDate.now()): LocalDate {
    return today.minusDays((today.dayOfWeek.value - 1).toLong())
}
