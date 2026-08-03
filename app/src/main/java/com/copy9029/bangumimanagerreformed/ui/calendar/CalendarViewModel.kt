package com.copy9029.bangumimanagerreformed.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.CalendarInactiveVisibilityDefaults
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
import java.time.temporal.ChronoUnit
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
            firstDayInclusive = firstWeekStart,
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


/**
 * 将番剧及其日期锚点展开为 CalendarScreen 可以直接按日期读取的稀疏映射。
 *
 * Schedule 不是逐集记录，而是“从某一集开始恢复每周播出”的锚点。因此计算时会把
 * 相邻锚点之间视为一个独立分段，再只生成落在当前日历窗口内的集数。
 */
private fun calcBangumisByDateMap(
    bangumis: List<Bangumi>,
    schedules: List<BangumiSchedule>,
    firstDayInclusive: LocalDate,
    dayCount: Int,

    calendarInactiveVisibility: Int,
    calendarFinishedEpisodeVisible: Boolean,
    calendarFinishedBangumiVisible: Boolean,
): Map<LocalDate, List<CalendarBangumiItemUiState>> {
    if (dayCount <= 0) return emptyMap()

    val lastDayExclusive = firstDayInclusive.plusDays(dayCount.toLong())

    val schedulesByBangumiId = schedules.groupBy(BangumiSchedule::bangumiId)

    val itemsByDate = mutableMapOf<LocalDate, MutableList<CalendarBangumiItemUiState>>()

    val filteredBangumis = bangumis.filter { bangumi ->
        val matchesInactiveVisibility = when (calendarInactiveVisibility) {
            CalendarInactiveVisibilityDefaults.ACTIVE -> bangumi.isActive
            CalendarInactiveVisibilityDefaults.ALL -> true
            CalendarInactiveVisibilityDefaults.INACTIVE -> !bangumi.isActive
            else -> bangumi.isActive
        }
        val isFinishedBangumi = bangumi.totalEpisodes?.let { totalEpisodes ->
            totalEpisodes > 0 && bangumi.latestWatchedEpisode >= totalEpisodes
        } == true

        matchesInactiveVisibility &&
            (calendarFinishedBangumiVisible || !isFinishedBangumi)
    }

    filteredBangumis.forEach { bangumi ->
        // 锚点按集数升序排列；每个锚点负责直到下一锚点前一集的播出计算。
        // 例如锚点位于第 1、5 集时，第一个锚点分段只负责第 1 至第 4 集。
        val orderedSchedules = schedulesByBangumiId[bangumi.bangumiId]
            .orEmpty()
            // 非正数集数没有日历含义；正常情况下数据库不会产生这种锚点。
            .filter { it.episodeId > 0 }
            .sortedBy(BangumiSchedule::episodeId)
        // 没有有效锚点时，无法推导这部番剧任意一集的播出日期。
        if (orderedSchedules.isEmpty()) return@forEach

        // 总集数未知时，仅由日历窗口限制生成数量；已知时不能生成最终集之后的项目。
        val finalEpisodeId = bangumi.totalEpisodes ?: Int.MAX_VALUE
        if (finalEpisodeId <= 0) return@forEach

        orderedSchedules.forEachIndexed { anchorIndex, anchor ->
            // 超过最终集的锚点不会影响这部番剧的日历结果。
            if (anchor.episodeId > finalEpisodeId) return@forEachIndexed

            // 当前分段不包含下一锚点本身，避免同一集被两个锚点重复计算。
            val nextAnchorEpisodeId = orderedSchedules
                .getOrNull(anchorIndex + 1)
                ?.episodeId
            val segmentLastEpisodeId = minOf(
                finalEpisodeId,
                nextAnchorEpisodeId?.minus(1) ?: Int.MAX_VALUE,
            )
            // 防御异常或重复锚点；正常的严格升序数据不会进入这个分支。
            if (segmentLastEpisodeId < anchor.episodeId) return@forEachIndexed

            // 直接跳到不早于 firstDay 的第一周，避免从多年前的锚点逐周遍历。
            val daysUntilFirstDay = ChronoUnit.DAYS.between(
                anchor.broadcastDate,
                firstDayInclusive,
            )
            val weeksToFirstCandidate = if (daysUntilFirstDay <= 0L) {
                0L
            } else {
                // 正数天数除以 7 时向上取整，保证候选日期不会仍位于窗口之前。
                (daysUntilFirstDay + 6L) / 7L
            }
            // 在正常周播分段中，跳过多少周就等价于跳过多少集。
            val firstCandidateEpisodeId = anchor.episodeId.toLong() +
                weeksToFirstCandidate
            if (
                firstCandidateEpisodeId > segmentLastEpisodeId.toLong() ||
                    firstCandidateEpisodeId > Int.MAX_VALUE.toLong()
            ) {
                // 候选集越过当前分段，或无法安全转换为实体使用的 Int 集数。
                return@forEachIndexed
            }

            // 在当前锚点分段内，每增加一集就向后移动一周。
            var episodeId = firstCandidateEpisodeId.toInt()
            var broadcastDate = anchor.broadcastDate.plusWeeks(weeksToFirstCandidate)
            while (
                episodeId <= segmentLastEpisodeId &&
                broadcastDate.isBefore(lastDayExclusive)
            ) {
                if (!broadcastDate.isBefore(firstDayInclusive)) {
                    // 同日多更会将同一番剧的多个集数加入同一个日期列表。
                    itemsByDate.getOrPut(broadcastDate) { mutableListOf() }
                        .add(
                            CalendarBangumiItemUiState(
                                bangumiId = bangumi.bangumiId,
                                episodeId = episodeId,
                                title = bangumi.title,
                                themeColorLong = bangumi.themeColorLong,
                                // 观看进度按“已经连续看完到第几集”解释。
                                isDone = episodeId <= bangumi.latestWatchedEpisode,
                                isActive = bangumi.isActive,
                            )
                        )
                }

                // 避免 Int.MAX_VALUE 加一溢出；一般只会在总集数未知时触及。
                if (episodeId == Int.MAX_VALUE) break
                episodeId += 1
                broadcastDate = broadcastDate.plusWeeks(1L)
            }
        }
    }

    val firstBroadcastDateByBangumiId = filteredBangumis.associate { bangumi ->
        bangumi.bangumiId to bangumi.firstBroadcastDate
    }
    val itemComparator = compareBy<CalendarBangumiItemUiState> { item ->
        if (item.isDone) 0 else 1
    }.thenByDescending { item ->
        firstBroadcastDateByBangumiId.getValue(item.bangumiId)
    }
    // TODO: 在完成状态、首播日期之后，通过 thenBy/thenByDescending 追加排序选项。

    return itemsByDate.mapNotNull { (date, items) ->
        // 日期归属已经确定；这里的筛选和排序只处理同一天内部的展示内容。
        val visibleItems = items.filter { item ->
            calendarFinishedEpisodeVisible || !item.isDone
        }
        visibleItems
            .takeIf { it.isNotEmpty() }
            ?.sortedWith(itemComparator)
            ?.let { sortedItems -> date to sortedItems }
    }.toMap()
}



private fun currentWeekStart(today: LocalDate = LocalDate.now()): LocalDate {
    return today.minusDays((today.dayOfWeek.value - 1).toLong())
}
