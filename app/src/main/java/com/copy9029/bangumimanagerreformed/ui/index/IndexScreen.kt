package com.copy9029.bangumimanagerreformed.ui.index

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiDetailDialog
import com.copy9029.bangumimanagerreformed.util.generateBangumiColorScheme
import kotlin.random.Random


data class SortAndFilterStatus (
    val seasonYear: Int?,
    val seasonMonth: Int?,
    val watchedTag: WatchedTags,
    val inactiveTag: InactiveTags,
    val sortTag: SortTags,
    val sortOrder: SortOrders,
)

val FocusingUpdatingStatus = SortAndFilterStatus(
    seasonYear = null,
    seasonMonth = null,
    watchedTag = WatchedTags.ALL,
    inactiveTag = InactiveTags.ACTIVE,
    sortTag = SortTags.FOCUSING_UPDATE_MODE,
    sortOrder = SortOrders.DESC,
)

val UnfocusingUpdatingStatus = SortAndFilterStatus(
    seasonYear = null,
    seasonMonth = null,
    watchedTag = WatchedTags.ALL,
    inactiveTag = InactiveTags.ACTIVE,
    sortTag = SortTags.BY_RECENT_UPDATE,
    sortOrder = SortOrders.DESC,
)

@Composable
fun IndexScreen(
    viewModel: IndexViewModel,
    onEditClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    IndexScreenContent(
        uiState = uiState,
        modifier = modifier,
        onFocusingUpdatingChanged = viewModel::onFocusingUpdatingChanged,
        onSortTagSelected = viewModel::onSortTagSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onOpenMoreFilters = viewModel::onOpenFilterSheet,
        onBangumiClick = viewModel::onBangumiClick,
        onAdd1BangumiClick = viewModel::onAdd1BangumiClick,
        onItemMoreClick = viewModel::onItemMoreClick,
        onEditClick = onEditClick,
        onFilterStatusChanged = viewModel::onFilterStatusChanged,
        onDismissFilterSheet = viewModel::onDismissFilterSheet,
        onDismissDetailDialog = viewModel::onDismissDetailDialog,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndexScreenContent(
    uiState: IndexUiState,
    modifier: Modifier = Modifier,

    onFocusingUpdatingChanged: (Boolean) -> Unit,
    onSortTagSelected: (SortTags) -> Unit,
    onSortOrderSelected: (SortOrders) -> Unit,
    onOpenMoreFilters: () -> Unit,

    onBangumiClick: (Int) -> Unit,
    onAdd1BangumiClick: (Int) -> Unit,
    onItemMoreClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,

    onFilterStatusChanged: (SortAndFilterStatus) -> Unit,
    onDismissFilterSheet: () -> Unit,
    onDismissDetailDialog: () -> Unit,

    ) {

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

            SortAndFilterRow(
                isFocusingUpdating = uiState.isFocusingUpdating,
                selectedSortTag = uiState.sortAndFilterStatus.sortTag,
                selectedSortOrder = uiState.sortAndFilterStatus.sortOrder,
                onFocusingUpdatingChanged = onFocusingUpdatingChanged,
                onSortTagSelected = onSortTagSelected,
                onSortOrderSelected = onSortOrderSelected,
                onOpenMoreFilters = onOpenMoreFilters,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(1.dp)
            ) {
                items(
                    items = uiState.bangumiList,
                    key = { it.bangumiIdInt }
                ) { item ->
                    IndexScreenItem(
                        uiState = item,
                        onClick = { onBangumiClick(item.bangumiIdInt) },
                        onAdd1Click = { onAdd1BangumiClick(item.bangumiIdInt) },
                        onEditClick = onEditClick,
                        onMoreClick = { onItemMoreClick(item.bangumiIdInt) },
                    )
                }
            }

            if (uiState.isFilterSheetVisible) {
                FilterBottomSheet(
                    status = uiState.sortAndFilterStatus,
                    onStatusChange = onFilterStatusChanged,
                    onDismissRequest = onDismissFilterSheet,
                    startYear = uiState.filterStartYear,
                    endYear = uiState.filterEndYear,
                )
            }

            uiState.bangumiDetailSelected?.let { detail ->
                BangumiDetailDialog(
                    uiState = detail,
                    onDismissRequest = onDismissDetailDialog,
                    onEditClick = {
                        onDismissDetailDialog()
                        onEditClick(detail.inProjectIDInt)
                    },
                )
            }

        }
    }
}


@Composable
private fun IndexScreenItem(
    uiState: BangumiIndexItemUiState,

    onClick: () -> Unit,
    onAdd1Click: () -> Unit,
    onEditClick: (Int) -> Unit,
    onMoreClick: () -> Unit,

    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(uiState.themeColorLong)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorScheme.border),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.cardContainer,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = uiState.titleStr,
                    color = colorScheme.primaryContent,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = uiState.watchProgressStr,
                    color = colorScheme.secondaryContent,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

//                Text(
//                    text = "${bangumi.seasonYear} 年 ${bangumi.seasonMonth} 月 丨 " +
//                            "评分：" + (bangumi.myScore?.div(10.0)?.toString() ?: "未知"),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val buttonBorderColor = colorScheme.border
                val buttonColors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colorScheme.buttonContainer,
                    contentColor = colorScheme.primaryContent,
                )

                CircleActionButton(
                    onClick = onAdd1Click,
                    borderColor = buttonBorderColor,
                    colors = buttonColors,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.index_item_button_plus_1),
                        contentDescription = "+1",
                        modifier = Modifier.size(18.dp)
                    )
                }

                CircleActionButton(
                    onClick = {
                        onEditClick(uiState.bangumiIdInt)
                    },
                    borderColor = buttonBorderColor,
                    colors = buttonColors,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(18.dp)
                    )
                }

                CircleActionButton(
                    onClick = onMoreClick,
                    borderColor = buttonBorderColor,
                    colors = buttonColors,
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "更多",
                        modifier = Modifier.size(18.dp)
                    )
                }

            }
        }
    }
}

@Composable
private fun CircleActionButton(
    onClick: () -> Unit,
    borderColor: Color,
    colors: IconButtonColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = modifier
            .size(30.dp),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor,
        ),
        colors = colors,
        shape = CircleShape,
        content = content,
    )
}



@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun PreviewHere() {
    IndexScreenContent(
        uiState = IndexUiState(
            bangumiList = List(10) { index ->
                BangumiIndexItemUiState(
                    titleStr = "Bangumi Title ${index + 1}".repeat(index + 1),
                    watchProgressStr = "已看完第 1 话 丨 更新到第 12 话",
                    themeColorLong = listOf(0xFFFFFFFF, 0xFFFF0000, 0xFF0000FF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF)[index.rem(6)],
                    bangumiIdInt = index + 1,
                )
            },
            isLoading = false,
        ),
        onFocusingUpdatingChanged = {},
        onSortTagSelected = {},
        onSortOrderSelected = {},
        onOpenMoreFilters = {},
        onBangumiClick = {},
        onAdd1BangumiClick = {},
        onItemMoreClick = {},
        onEditClick = {},
        onFilterStatusChanged = {},
        onDismissFilterSheet = {},
        onDismissDetailDialog = {}
    )
}
