package com.copy9029.bangumimanagerreformed.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiDetailDialogUiState
import com.copy9029.bangumimanagerreformed.ui.bangumi.toDetailDialogUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CalendarBangumiItemUiState(
    val bangumiId: Int,
    val episodeId: Int,
    val title: String,
    val themeColorLong: Long,
    val isDone: Boolean,
    val isActive: Boolean = true,
)

private data class CalendarDateMapData(
    val bangumis: List<Bangumi>,
    val schedulesByBangumiId: Map<Int, List<BangumiSchedule>>,
    val firstWeekStart: LocalDate,
    val weekCount: Int,
    val weeksPrefix: Int,
    val initialWeekIndex: Int,
    val bangumisByDate: Map<LocalDate, List<CalendarBangumiItemUiState>>,
)

data class CalendarUiState(

    val firstWeekStart: LocalDate = currentWeekStart()
        .minusWeeks(SettingsRepository.DEFAULT_CALENDAR_WEEKS_BEFORE_CURRENT.toLong()),
    val weekCount: Int = SettingsRepository.DEFAULT_CALENDAR_WEEKS_BEFORE_CURRENT +
        SettingsRepository.DEFAULT_CALENDAR_WEEKS_AFTER_CURRENT + 1,
    val weeksPrefix: Int = SettingsRepository.DEFAULT_CALENDAR_WEEKS_PREFIX,
    val initialWeekIndex: Int =
        SettingsRepository.DEFAULT_CALENDAR_WEEKS_BEFORE_CURRENT - weeksPrefix,

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
    private val bangumiRepository: BangumiRepository,
    private val settingsRepository: SettingsRepository,
): ViewModel() {

    private val _selectedDateEpochDay = MutableStateFlow<Long?>(null)
    private val _selectedBangumiId = MutableStateFlow<Int?>(null)

    private val calendarDateMapFlow = combine(
        bangumiRepository.getAllBangumis().distinctUntilChanged(),
        bangumiRepository.getAllSchedules().distinctUntilChanged(),
        settingsRepository.calendarSettings.distinctUntilChanged(),
    ) { bangumis, schedules, settings ->
        val weeksBeforeCurrent = settings.calendarWeeksBeforeCurrent
        val weeksAfterCurrent = settings.calendarWeeksAfterCurrent
        val weeksPrefix = settings.calendarWeeksPrefix
        val firstWeekStart = currentWeekStart().minusWeeks(weeksBeforeCurrent.toLong())
        val weekCount = weeksBeforeCurrent + weeksAfterCurrent + 1
        val initialWeekIndex = (weeksBeforeCurrent - weeksPrefix)
            .coerceIn(0, weekCount - 1)

        val bangumisByDate = calcBangumisByDateMap(
            bangumis = bangumis,
            schedules = schedules,
            firstDay = firstWeekStart,
            dayCount = weekCount * 7,
            calendarInactiveVisibility = settings.calendarInactiveVisibility,
            calendarFinishedEpisodeVisible = settings.calendarFinishedEpisodeVisible,
            calendarFinishedBangumiVisible = settings.calendarFinishedBangumiVisible,
        )
        val schedulesByBangumiId = schedules.groupBy { it.bangumiId }

        CalendarDateMapData(
            bangumis = bangumis,
            schedulesByBangumiId = schedulesByBangumiId,
            firstWeekStart = firstWeekStart,
            weekCount = weekCount,
            weeksPrefix = weeksPrefix,
            initialWeekIndex = initialWeekIndex,
            bangumisByDate = bangumisByDate,
        )
    }.flowOn(Dispatchers.Default)

    val uiState: StateFlow<CalendarUiState> = combine(
        calendarDateMapFlow,
        _selectedDateEpochDay,
        _selectedBangumiId,
    ) { calendarData, selectedDateEpochDay, selectedBangumiId ->
        val selectedBangumi = calendarData.bangumis.firstOrNull {
            it.bangumiId == selectedBangumiId
        }

        CalendarUiState(
            firstWeekStart = calendarData.firstWeekStart,
            weekCount = calendarData.weekCount,
            weeksPrefix = calendarData.weeksPrefix,
            initialWeekIndex = calendarData.initialWeekIndex,
            bangumisByDate = calendarData.bangumisByDate,
            selectedDateEpochDay = selectedDateEpochDay,
            bangumiDetailSelected = selectedBangumi?.toDetailDialogUiState(
                schedules = calendarData.schedulesByBangumiId[
                    selectedBangumi.bangumiId
                ].orEmpty(),
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
        viewModelScope.launch {
            bangumiRepository.watchedEpisodeSet(bangumiId, episodeId)
        }
    }

    fun onMarkEpisodeUndoneClick(bangumiId: Int, episodeId: Int) {
        viewModelScope.launch {
            bangumiRepository.watchedEpisodeSet(bangumiId, (episodeId - 1).coerceAtLeast(0))
        }
    }

    fun onToggleBangumiActiveClick(bangumiId: Int) {
        viewModelScope.launch {
            bangumiRepository.toggleBangumiActive(bangumiId)
        }
    }

    fun onDeleteBangumiClick(bangumiId: Int) {
        viewModelScope.launch {
            bangumiRepository.deleteBangumi(bangumiId)
        }
    }
}


private fun calcBangumisByDateMap(
    bangumis: List<Bangumi>,
    schedules: List<BangumiSchedule>,
    firstDay: LocalDate,
    dayCount: Int,

    calendarInactiveVisibility: Int,
    calendarFinishedEpisodeVisible: Boolean,
    calendarFinishedBangumiVisible: Boolean,
): Map<LocalDate, List<CalendarBangumiItemUiState>> {

    // TODO：注意筛选/排序
    return emptyMap()
}



private fun currentWeekStart(today: LocalDate = LocalDate.now()): LocalDate {
    return today.minusDays((today.dayOfWeek.value - 1).toLong())
}
