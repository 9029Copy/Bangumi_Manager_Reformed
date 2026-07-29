package com.copy9029.bangumimanagerreformed.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiDetailDialogUiState
import com.copy9029.bangumimanagerreformed.ui.bangumi.toDetailDialogUiState
import com.copy9029.bangumimanagerreformed.util.calcBangumisByDateMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// TODO: move into settings
private const val WEEKS_BEFORE_CURRENT = 520
private const val WEEKS_AFTER_CURRENT = 520
const val WEEKS_PREFIX = 1

data class CalendarBangumiItemUiState(
    val bangumiId: Int,
    val episodeId: Int,
    val title: String,
    val themeColorLong: Long,
    val isDone: Boolean,
    val isActive: Boolean = true,
)

data class CalendarUiState(

    val firstWeekStart: LocalDate = currentWeekStart()
        .minusWeeks(WEEKS_BEFORE_CURRENT.toLong()),
    val weekCount: Int = WEEKS_BEFORE_CURRENT + WEEKS_AFTER_CURRENT + 1,
    val initialWeekIndex: Int = WEEKS_BEFORE_CURRENT - WEEKS_PREFIX,

    val bangumisByDate: Map<LocalDate, List<CalendarBangumiItemUiState>> = emptyMap(),
    val selectedDateEpochDay: Long? = null,
    val bangumiDetailSelected: BangumiDetailDialogUiState? = null,
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
    private val repository: BangumiRepository,
): ViewModel() {

    private val _selectedDateEpochDay = MutableStateFlow<Long?>(null)
    private val _selectedBangumiId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.getAllBangumis(),
        repository.getAllSchedules(),
        _selectedDateEpochDay,
        _selectedBangumiId,
    ) { bangumis, schedules, selectedDateEpochDay, selectedBangumiId ->

        // TODO: move into settings
        val firstWeekStart = currentWeekStart().minusWeeks(WEEKS_BEFORE_CURRENT.toLong())
        val weekCount = WEEKS_BEFORE_CURRENT + WEEKS_AFTER_CURRENT + 1
        val initialWeekIndex = WEEKS_BEFORE_CURRENT - WEEKS_PREFIX

        val bangumisByDate = calcBangumisByDateMap(
            bangumis = bangumis,
            schedules = schedules,
            firstDay = firstWeekStart,
            dayCount = weekCount * 7,
        )
        val schedulesByBangumiId = schedules.groupBy { it.bangumiId }
        val selectedBangumi = bangumis.firstOrNull {
            it.bangumiId == selectedBangumiId
        }

        CalendarUiState(
            firstWeekStart = firstWeekStart,
            weekCount = weekCount,
            initialWeekIndex = initialWeekIndex,
            bangumisByDate = bangumisByDate,
            selectedDateEpochDay = selectedDateEpochDay,
            bangumiDetailSelected = selectedBangumi?.toDetailDialogUiState(
                schedules = schedulesByBangumiId[selectedBangumi.bangumiId].orEmpty(),
            ),
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

    fun onBangumiClick(bangumiId: Int) {
        _selectedBangumiId.value = bangumiId
    }

    fun onDismissDetailDialog() {
        _selectedBangumiId.value = null
    }

    fun onMarkEpisodeDoneClick(bangumiId: Int, episodeId: Int) {
        // TODO: 将指定集数标记为已完成。
    }

    fun onMarkEpisodeUndoneClick(bangumiId: Int, episodeId: Int) {
        // TODO: 将指定集数标记为未完成。
    }

    fun onToggleBangumiActiveClick(bangumiId: Int) {
        viewModelScope.launch {
            repository.toggleBangumiActive(bangumiId)
        }
    }

    fun onDeleteBangumiClick(bangumiId: Int) {
        viewModelScope.launch {
            repository.deleteBangumi(bangumiId)
        }
    }
}

private fun currentWeekStart(today: LocalDate = LocalDate.now()): LocalDate {
    return today.minusDays((today.dayOfWeek.value - 1).toLong())
}
