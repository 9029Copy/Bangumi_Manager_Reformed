package com.copy9029.bangumimanagerreformed.ui.index

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.ui.BangumiViewModel
import java.time.LocalDate
import java.time.YearMonth


data class SortAndFilterStatus (
    val seasonYear: Int?,
    val seasonMonth: Int?,
    val updateTag: UpdateTags,
    val watchedTag: WatchedTags,
    val sortTag: SortTags,
    val sortOrder: SortOrders,
)

private val FocusingUpdatingStatus = SortAndFilterStatus(
    seasonYear = null,
    seasonMonth = null,
    updateTag = UpdateTags.UPDATING,
    watchedTag = WatchedTags.ALL,
    sortTag = SortTags.BY_START_TIME,
    sortOrder = SortOrders.ASC,
)

private val NormalStatus = SortAndFilterStatus(
    seasonYear = null,
    seasonMonth = null,
    updateTag = UpdateTags.ALL,
    watchedTag = WatchedTags.ALL,
    sortTag = SortTags.BY_START_TIME,
    sortOrder = SortOrders.ASC,
)

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(
//    viewModel: ...,
    modifier: Modifier = Modifier,

) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "IndexScreen", fontSize = 20.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },

    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            var sortAndFilterStatus by remember {
                mutableStateOf(FocusingUpdatingStatus)
            }

            var showFilterSheet by remember {
                mutableStateOf(false)
            }

            val isFocusingUpdating =
                sortAndFilterStatus.seasonYear == null &&
                sortAndFilterStatus.seasonMonth == null &&
                sortAndFilterStatus.updateTag == UpdateTags.UPDATING &&
                sortAndFilterStatus.watchedTag == WatchedTags.ALL

            // temporary
            val bangumisToShow = remember {
                listOf(
                    Bangumi(
                        bangumiId = 1,
                        title = "bangumi title 1",
                        seasonYear = 2026,
                        seasonMonth = 7,
                        myScore = 100,
                        firstBroadcastDate = LocalDate.of(2026, 7, 1),
                        totalEpisodes = null,
                        latestWatchedEpisode = 2,
                        expectedEndDate = null,
                    ),
                    Bangumi(
                        bangumiId = 2,
                        title = "bangumi title 2",
                        seasonYear = 2026,
                        seasonMonth = 7,
                        myScore = 100,
                        firstBroadcastDate = LocalDate.of(2026, 7, 2),
                        totalEpisodes = null,
                        latestWatchedEpisode = 3,
                        expectedEndDate = null,
                    )
                )
            }

            SortAndFilterRow(
                isFocusingUpdating = isFocusingUpdating,
                selectedSortTag = sortAndFilterStatus.sortTag,
                selectedSortOrder = sortAndFilterStatus.sortOrder,
                onFocusingUpdatingChanged = { checked ->
                    sortAndFilterStatus = if (checked) {
                        FocusingUpdatingStatus
                    } else {
                        NormalStatus
                    }
                },
                onSortTagSelected = {
                    sortAndFilterStatus = sortAndFilterStatus.copy(sortTag = it)
                },
                onSortOrderSelected = {
                    sortAndFilterStatus = sortAndFilterStatus.copy(sortOrder = it)
                },
                onOpenMoreFilters = {
                    showFilterSheet = true
                },
            )

            if (showFilterSheet) {
                FilterBottomSheet(
                    status = sortAndFilterStatus,
                    onStatusChange = {
                        sortAndFilterStatus = it
                    },
                    onDismissRequest = {
                        showFilterSheet = false
                    },
                    startYear = 2025,   // TODO: get this from ViewModel
                    endYear = 2026,     // TODO: get this from ViewModel
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // 如果需要单元格之间有分割线，可以加 contentPadding
                contentPadding = PaddingValues(1.dp)
            ) {
                items(
                    items = bangumisToShow,
                    key = { it.bangumiId }
                ) { bangumi ->
                    IndexScreenItem(
                        bangumi = bangumi,
                        onClick = {
                            // TODO: 进入详情页
                        },
                    )
                }
            }

        }
    }
}




@Composable
private fun IndexScreenItem(
    bangumi: Bangumi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(80.dp).padding(5.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, Color.LightGray)
    ) {
        Column() {
            Text(
                text = bangumi.title,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}


