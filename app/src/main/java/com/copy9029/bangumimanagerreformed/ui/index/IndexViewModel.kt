package com.copy9029.bangumimanagerreformed.ui.index

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.GlobalSettings
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiDetailDialogUiState
import com.copy9029.bangumimanagerreformed.ui.bangumi.toDetailDialogUiState
import com.copy9029.bangumimanagerreformed.util.buildBangumiWatchProgressText
import com.copy9029.bangumimanagerreformed.util.latestAiredBroadcastDate
import com.copy9029.bangumimanagerreformed.util.latestAiredEpisode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class IndexUiState(
    val bangumiList: List<BangumiIndexItemUiState> = emptyList(),

    val sortAndFilterStatus: SortAndFilterStatus = FocusingUpdatingStatus,
    val isFocusingUpdating: Boolean = true,

    val isFilterSheetVisible: Boolean = false,
    val filterStartYear: Int = 2025,
    val filterEndYear: Int = 2027,

    val bangumiDetailSelected: BangumiDetailDialogUiState? = null,  // null -> invisible, notnull -> visible

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val filteredItemCount: Int
        get() = bangumiList.size
}

data class BangumiIndexItemUiState(
    val titleStr: String,
    val watchProgressStr: String,
    val themeColorLong: Long,

    val bangumiIdInt: Int,
    val isActive: Boolean,
)

private data class IndexBangumiData(
    val bangumis: List<Bangumi>,
    val schedules: List<BangumiSchedule>,
    val globalSettings: GlobalSettings,
)

@HiltViewModel
class IndexViewModel @Inject constructor(
    private val repository: BangumiRepository,
    private val settingsRepository: SettingsRepository,
): ViewModel() {

    private val _sortAndFilterStatus = MutableStateFlow(FocusingUpdatingStatus)
    private val _isFilterSheetVisible = MutableStateFlow(false)
    private val _selectedBangumiId = MutableStateFlow<Int?>(null)

    private val indexBangumiDataFlow = combine(
        repository.getAllBangumis(),
        repository.getAllSchedules(),
        settingsRepository.globalSettings,
    ) { bangumis, schedules, globalSettings ->
        IndexBangumiData(
            bangumis = bangumis,
            schedules = schedules,
            globalSettings = globalSettings,
        )
    }

    val uiState: StateFlow<IndexUiState> = combine(
        indexBangumiDataFlow,
        _sortAndFilterStatus,
        _isFilterSheetVisible,
        _selectedBangumiId,
    ) { bangumiData, sortAndFilterStatus, filterSheetVisible, selectedId ->
        val bangumis = bangumiData.bangumis
        val schedules = bangumiData.schedules
        val globalSettings = bangumiData.globalSettings

        val schedulesByBangumiId = schedules.groupBy { it.bangumiId }

        val filteredBangumis = bangumis
            .filterByStatus(sortAndFilterStatus)
            .sortByStatus(sortAndFilterStatus, schedulesByBangumiId)

        val itemUiStates = filteredBangumis.map { bangumi ->
            bangumi.toIndexItemUiState(
                schedules = schedulesByBangumiId[bangumi.bangumiId].orEmpty(),
                themeColorLong = globalSettings.colorForSeasonMonth(bangumi.seasonMonth),
            )
        }

        val selectedBangumi = bangumis.firstOrNull {
            it.bangumiId == selectedId
        }

        IndexUiState(
            bangumiList = itemUiStates,
            sortAndFilterStatus = sortAndFilterStatus,
            isFocusingUpdating = sortAndFilterStatus == FocusingUpdatingStatus,
            isFilterSheetVisible = filterSheetVisible,
            filterStartYear = bangumis.minOfOrNull { it.seasonYear } ?: 2025,
            filterEndYear = bangumis.maxOfOrNull { it.seasonYear } ?: 2027,
            bangumiDetailSelected = selectedBangumi?.toDetailDialogUiState(
                schedules = schedulesByBangumiId[selectedBangumi.bangumiId].orEmpty(),
                themeColorLong = globalSettings.colorForSeasonMonth(
                    selectedBangumi.seasonMonth
                ),
            ),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = IndexUiState(isLoading = true),
    )

    fun onFocusingUpdatingChanged(checked: Boolean) {
        _sortAndFilterStatus.value = if (checked) {
            FocusingUpdatingStatus
        } else {
            UnfocusingUpdatingStatus
        }
    }

    fun onSortTagSelected(sortTag: SortTags) {
        _sortAndFilterStatus.value =
            _sortAndFilterStatus.value.copy(sortTag = sortTag)
    }

    fun onSortOrderSelected(sortOrder: SortOrders) {
        _sortAndFilterStatus.value =
            _sortAndFilterStatus.value.copy(sortOrder = sortOrder)
    }

    fun onFilterStatusChanged(status: SortAndFilterStatus) {
        _sortAndFilterStatus.value = status
    }

    fun onOpenFilterSheet() {
        _isFilterSheetVisible.value = true
    }

    fun onDismissFilterSheet() {
        _isFilterSheetVisible.value = false
    }

    fun onBangumiClick(bangumiId: Int) {
        _selectedBangumiId.value = bangumiId
    }

    fun onDismissDetailDialog() {
        _selectedBangumiId.value = null
    }

    fun onAdd1BangumiClick(bangumiId: Int) {
        viewModelScope.launch {
            repository.watchedEpisodeAdd(bangumiId, 1)
        }
    }

    fun onMinus1BangumiClick(bangumiId: Int) {
        viewModelScope.launch {
            repository.watchedEpisodeAdd(bangumiId, -1)
        }
    }

    fun onSetBangumiActiveClick(bangumiId: Int) {
        viewModelScope.launch {
            repository.toggleBangumiActive(bangumiId)
        }
    }

    fun onDeleteBangumiClick(bangumiId: Int) {
        viewModelScope.launch {
            repository.deleteBangumi(bangumiId)
        }
    }

    fun onTopSettingsClick() {
        // TODO
    }

}



private fun List<Bangumi>.filterByStatus(
    sortAndFilterStatus: SortAndFilterStatus,
//    schedulesByBangumiId: Map<Int, List<BangumiSchedule>>
): List<Bangumi> {
    val today = LocalDate.now()

    return this
        .filter { bangumi ->
            sortAndFilterStatus.seasonYear == null ||
                    bangumi.seasonYear == sortAndFilterStatus.seasonYear
        }
        .filter { bangumi ->
            sortAndFilterStatus.seasonMonth == null ||
                    bangumi.seasonMonth == sortAndFilterStatus.seasonMonth
        }
        .filter { bangumi ->
            when (sortAndFilterStatus.watchedTag) {
                WatchedTags.ALL -> true
                WatchedTags.FINISHED -> bangumi.isFinished(today)   // 已看完: 仅"已完结已看完"
                WatchedTags.UNFINISHED -> !bangumi.isFinished(today) // 未看完: 包括“已完结未看完”和“未完结”
            }
        }
        .filter { bangumi ->
            when (sortAndFilterStatus.inactiveTag) {
                InactiveTags.ALL -> true
                InactiveTags.ACTIVE -> bangumi.isActive
                InactiveTags.INACTIVE -> !bangumi.isActive
            }
        }
}


private fun List<Bangumi>.sortByStatus(
    sortAndFilterStatus: SortAndFilterStatus,
    schedulesByBangumiId: Map<Int, List<BangumiSchedule>>
): List<Bangumi> {
    val today = LocalDate.now()
    val sorted = when (sortAndFilterStatus.sortTag) {
        SortTags.FOCUSING_UPDATE_MODE -> {
            sortedWith(
                compareByDescending<Bangumi> { bangumi ->
                    val latestAired = bangumi.latestAiredEpisode(
                        schedules = schedulesByBangumiId[bangumi.bangumiId].orEmpty(),
                        today = today,
                    )

                    bangumi.latestWatchedEpisode < latestAired
                }.thenByDescending { bangumi ->
                    bangumi.latestAiredBroadcastDate(
                        schedules = schedulesByBangumiId[bangumi.bangumiId].orEmpty(),
                        today = today,
                    ) ?: LocalDate.MIN
                }
            )
        }

        SortTags.BY_EDITED_ORDER -> {
            sortedWith(
                compareByDescending<Bangumi> {
                    it.lastBasicInfoModifiedAtMillis
                }.thenByDescending {
                    it.bangumiId
                }
            )
        }

        SortTags.BY_RECENT_UPDATE -> {
            sortedBy { bangumi ->
                bangumi.latestAiredBroadcastDate(
                    schedules = schedulesByBangumiId[bangumi.bangumiId].orEmpty(),
                    today = today,
                ) ?: LocalDate.MIN
            }
        }

        SortTags.BY_START_TIME -> {
            sortedBy { it.firstBroadcastDate }
        }
    }

    return when (sortAndFilterStatus.sortOrder) {
        SortOrders.ASC -> sorted.asReversed()
        SortOrders.DESC -> sorted
    }
}


private fun Bangumi.toIndexItemUiState(
    schedules: List<BangumiSchedule>,
    themeColorLong: Long,
): BangumiIndexItemUiState {
    val today = LocalDate.now()
    val latestAiredEpisode = latestAiredEpisode(
        schedules = schedules,
        today = today,
    )
    val latestAiredDate = latestAiredBroadcastDate(
        schedules = schedules,
        today = today,
    )
    return BangumiIndexItemUiState(
        titleStr = title,
        watchProgressStr = buildBangumiWatchProgressText(
            dayOfWeekInt = latestAiredDate?.dayOfWeek?.value ?: firstBroadcastDate.dayOfWeek.value,
            latestWatchedEpisode = latestWatchedEpisode,
            latestAiredEpisode = latestAiredEpisode,
            totalEpisodes = totalEpisodes,
            startDate = firstBroadcastDate,
        ),
        themeColorLong = themeColorLong,
        bangumiIdInt = bangumiId,
        isActive = isActive,
    )
}


/**
 * 是否看完
 */
private fun Bangumi.isFinished(today: LocalDate): Boolean {
    return if (this.isUpdating(today)) {
        false
    } else {
        latestWatchedEpisode >= totalEpisodes!!.toInt()
    }
}

/**
 * 是否正在连载中
 */
private fun Bangumi.isUpdating(today: LocalDate): Boolean {
    return expectedEndDate == null || !expectedEndDate.isBefore(today)
}



