package com.copy9029.bangumimanagerreformed.ui.profile.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.util.calcNearestSeason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

data class OverviewSeasonOptionUiState(
    val season: OverviewSeason,
    val itemCount: Int,
)

data class OverviewUiState(
    val selectedSeason: OverviewSeason = currentOverviewSeason(),
    val items: List<OverviewItemUiState> = emptyList(),
    val isLoading: Boolean = true,
    val seasonOptions: List<OverviewSeasonOptionUiState> = buildSeasonOptions(
        firstSeason = currentOverviewSeason(),
        lastSeason = nearestOverviewSeason(),
        itemCountBySeason = emptyMap(),
    ),
) {
    val previousSeason: OverviewSeason
        get() = selectedSeason.previous()

    val nextSeason: OverviewSeason
        get() = selectedSeason.next()
}

private data class OverviewSeasonItems(
    val season: OverviewSeason,
    val items: List<OverviewItemUiState>,
    val isLoading: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val bangumiRepository: BangumiRepository,
) : ViewModel() {
    private val selectedSeason = MutableStateFlow(currentOverviewSeason())

    private val seasonItems = selectedSeason.flatMapLatest { season ->
        bangumiRepository.getBangumisBySeason(
            seasonYear = season.year,
            seasonMonth = season.month,
        ).map { bangumis ->
            OverviewSeasonItems(
                season = season,
                items = bangumis.map(Bangumi::toOverviewItemUiState),
                isLoading = false,
            )
        }.onStart {
            emit(
                OverviewSeasonItems(
                    season = season,
                    items = emptyList(),
                    isLoading = true,
                ),
            )
        }
    }

    val uiState: StateFlow<OverviewUiState> = combine(
        selectedSeason,
        seasonItems,
        bangumiRepository.getBangumiSeasonCounts(),
    ) { selectedSeason, loadedSeasonItems, seasonCounts ->
        val today = LocalDate.now()
        val currentSeason = currentOverviewSeason(today)
        val nearestSeason = nearestOverviewSeason(today)
        val projectSeasons = seasonCounts.mapNotNull { seasonCount ->
            seasonCount.seasonMonth
                .takeIf(OVERVIEW_SEASON_MONTHS::contains)
                ?.let { month ->
                    OverviewSeason(
                        year = seasonCount.seasonYear,
                        month = month,
                    )
                }
        }
        val earliestProjectSeason = projectSeasons.minByOrNull(OverviewSeason::orderValue)
        val latestProjectSeason = projectSeasons.maxByOrNull(OverviewSeason::orderValue)
        val firstSeason = if (
            earliestProjectSeason == null ||
            currentSeason.orderValue() <= earliestProjectSeason.orderValue()
        ) {
            currentSeason
        } else {
            earliestProjectSeason
        }
        val lastSeason = if (
            latestProjectSeason == null ||
            nearestSeason.orderValue() >= latestProjectSeason.orderValue()
        ) {
            nearestSeason
        } else {
            latestProjectSeason
        }
        val isSelectedSeasonLoaded = loadedSeasonItems.season == selectedSeason
        val itemCountBySeason = seasonCounts.associate { seasonCount ->
            OverviewSeason(
                year = seasonCount.seasonYear,
                month = seasonCount.seasonMonth,
            ) to seasonCount.itemCount
        }

        OverviewUiState(
            selectedSeason = selectedSeason,
            items = if (isSelectedSeasonLoaded) {
                loadedSeasonItems.items
            } else {
                emptyList()
            },
            isLoading = !isSelectedSeasonLoaded || loadedSeasonItems.isLoading,
            seasonOptions = buildSeasonOptions(
                firstSeason = firstSeason,
                lastSeason = lastSeason,
                itemCountBySeason = itemCountBySeason,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OverviewUiState(
            selectedSeason = selectedSeason.value,
        ),
    )

    fun onPreviousSeasonClick() {
        selectedSeason.update(OverviewSeason::previous)
    }

    fun onNextSeasonClick() {
        selectedSeason.update(OverviewSeason::next)
    }

    fun onSeasonSelected(season: OverviewSeason) {
        require(season.month in OVERVIEW_SEASON_MONTHS) {
            "季度月份必须为 1、4、7 或 10"
        }
        selectedSeason.value = season
    }
}

private fun Bangumi.toOverviewItemUiState(): OverviewItemUiState {
    return OverviewItemUiState(
        bangumiId = bangumiId,
        title = title,
        scoreTimesTen = myScore,
    )
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

private fun nearestOverviewSeason(today: LocalDate = LocalDate.now()): OverviewSeason {
    val nearestSeason = calcNearestSeason(today)
    return OverviewSeason(
        year = nearestSeason.year,
        month = nearestSeason.monthValue,
    )
}

private fun buildSeasonOptions(
    firstSeason: OverviewSeason,
    lastSeason: OverviewSeason,
    itemCountBySeason: Map<OverviewSeason, Int>,
): List<OverviewSeasonOptionUiState> {
    val options = mutableListOf<OverviewSeasonOptionUiState>()
    var season = firstSeason

    while (season.orderValue() <= lastSeason.orderValue()) {
        options += OverviewSeasonOptionUiState(
            season = season,
            itemCount = itemCountBySeason[season] ?: 0,
        )
        season = season.next()
    }

    return options
}

private fun OverviewSeason.orderValue(): Long {
    return year.toLong() * 12L + month
}

internal val OVERVIEW_SEASON_MONTHS = listOf(1, 4, 7, 10)
