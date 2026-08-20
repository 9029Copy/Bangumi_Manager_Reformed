package com.copy9029.bangumimanagerreformed.ui.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.INACTIVE_COLOR_LONG
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiDetailDialog
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiWatchProgressUiState
import com.copy9029.bangumimanagerreformed.ui.bangumi.displayText
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheetViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheet
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.ui.theme.generateBangumiColorScheme
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import java.time.LocalDate


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
    watchedTag = WatchedTags.UNFINISHED,
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
    indexViewModel: IndexViewModel,
    bangumiAddSheetViewModel: BangumiAddSheetViewModel,
    onEditClick: (Int) -> Unit,
    onBatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by indexViewModel.uiState.collectAsStateWithLifecycle()

    var isAddSheetVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var pendingEditBangumiId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    LaunchedEffect(uiState.bangumiDetailSelected, pendingEditBangumiId) {
        val bangumiId = pendingEditBangumiId ?: return@LaunchedEffect
        if (uiState.bangumiDetailSelected == null) {
            pendingEditBangumiId = null
            onEditClick(bangumiId)
        }
    }

    IndexScreenContent(
        uiState = uiState,
        modifier = modifier,
        onFocusingUpdatingChanged = indexViewModel::onFocusingUpdatingChanged,
        onSortTagSelected = indexViewModel::onSortTagSelected,
        onSortOrderSelected = indexViewModel::onSortOrderSelected,
        onOpenMoreFilters = indexViewModel::onOpenFilterSheet,
        onBangumiClick = indexViewModel::onBangumiClick,
        onAdd1BangumiClick = indexViewModel::onAdd1BangumiClick,
        onEditClick = { bangumiId ->
            pendingEditBangumiId = bangumiId
            indexViewModel.onDismissDetailDialog()
        },
        onMinus1BangumiClick = indexViewModel::onMinus1BangumiClick,
        onSetBangumiActiveClick = indexViewModel::onSetBangumiActiveClick,
        onDeleteBangumiClick = indexViewModel::onDeleteBangumiClick,
        onFilterStatusChanged = indexViewModel::onFilterStatusChanged,
        onDismissFilterSheet = indexViewModel::onDismissFilterSheet,
        onDismissDetailDialog = indexViewModel::onDismissDetailDialog,
        onTopAddClick = {
            isAddSheetVisible = true
        },
        onTopSettingsClick = indexViewModel::onTopSettingsClick,
    )

    if (isAddSheetVisible) {
        BangumiAddSheet(
            viewModel = bangumiAddSheetViewModel,
            defaultFirstBroadcastDate = LocalDate.now(),
            onDismissRequest = {
                isAddSheetVisible = false
            },
            onBatchClick = {
                isAddSheetVisible = false
                onBatchClick()
            },
        )
    }
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
    onEditClick: (Int) -> Unit,
    onMinus1BangumiClick: (Int) -> Unit,
    onSetBangumiActiveClick: (Int) -> Unit,
    onDeleteBangumiClick: (Int) -> Unit,

    onFilterStatusChanged: (SortAndFilterStatus) -> Unit,
    onDismissFilterSheet: () -> Unit,
    onDismissDetailDialog: () -> Unit,

    onTopAddClick: () -> Unit,
    onTopSettingsClick: () -> Unit,
) {
    var expandedMoreMenuBangumiId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    var pendingSetActiveBangumiId by rememberSaveable {
        mutableStateOf<BangumiIndexItemUiState?>(null)
    }

    var pendingDeleteBangumiId by rememberSaveable {
        mutableStateOf<BangumiIndexItemUiState?>(null)
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun scrollToTop() {
        coroutineScope.launch {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = pluralStringResource(
                            R.plurals.index_item_count,
                            uiState.filteredItemCount,
                            uiState.filteredItemCount,
                        ),
                        fontSize = 20.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(
                        onClick = ::scrollToTop,
                        enabled = listState.canScrollBackward,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.index_arrow_upward_24),
                            contentDescription = stringResource(R.string.index_back_to_top),
                            modifier = Modifier.size(30.dp),
                        )
                    }
                    IconButton(
                        onClick = onTopAddClick
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.action_add_item),
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    IconButton(
                        onClick = onTopSettingsClick
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },

    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            SortAndFilterRow(
                isFocusingUpdating = uiState.isFocusingUpdating,
                selectedSortTag = uiState.sortAndFilterStatus.sortTag,
                selectedSortOrder = uiState.sortAndFilterStatus.sortOrder,
                onFocusingUpdatingChanged = { checked ->
                    onFocusingUpdatingChanged(checked)
                    scrollToTop()
                },
                onSortTagSelected = { sortTag ->
                    onSortTagSelected(sortTag)
                    scrollToTop()
                },
                onSortOrderSelected = { sortOrder ->
                    onSortOrderSelected(sortOrder)
                    scrollToTop()
                },
                onOpenMoreFilters = onOpenMoreFilters,
            )

            LazyColumnScrollbar(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                settings = ScrollbarSettings.Default.copy(
                    thumbThickness = 4.dp,
                    scrollbarPadding = 2.dp,
                    thumbSelectedColor = MaterialTheme.colorScheme.onSurface,
                    thumbUnselectedColor = MaterialTheme.colorScheme.outline,
                ),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(1.dp),
                ) {
                    items(
                        items = uiState.bangumiList,
                        key = { it.bangumiIdInt },
                    ) { item ->
                        IndexScreenItem(
                            uiState = item,
                            onClick = { onBangumiClick(item.bangumiIdInt) },
                            onAdd1Click = { onAdd1BangumiClick(item.bangumiIdInt) },
                            onEditClick = onEditClick,
                            isMoreMenuExpanded = expandedMoreMenuBangumiId == item.bangumiIdInt,
                            onMoreClick = {
                                expandedMoreMenuBangumiId = item.bangumiIdInt
                            },
                            onDismissMoreMenu = {
                                expandedMoreMenuBangumiId = null
                            },
                            onMinus1Click = {
                                expandedMoreMenuBangumiId = null
                                onMinus1BangumiClick(item.bangumiIdInt)
                            },
                            isActive = item.isActive,
                            onSetActiveClick = {
                                expandedMoreMenuBangumiId = null
                                pendingSetActiveBangumiId = item
                            },
                            onDeleteClick = {
                                expandedMoreMenuBangumiId = null
                                pendingDeleteBangumiId = item
                            },
                        )
                    }
                }
            }

            // 筛选表
            if (uiState.isFilterSheetVisible) {
                FilterBottomSheet(
                    status = uiState.sortAndFilterStatus,
                    onStatusChange = { status ->
                        onFilterStatusChanged(status)
                        scrollToTop()
                    },
                    onDismissRequest = onDismissFilterSheet,
                    startYear = uiState.filterStartYear,
                    endYear = uiState.filterEndYear,
                )
            }

            // 详细信息dialog
            uiState.bangumiDetailSelected?.let { detail ->
                BangumiDetailDialog(
                    uiState = detail,
                    onDismissRequest = onDismissDetailDialog,
                    onEditClick = {
                        onEditClick(detail.inProjectIDInt)
                    },
                )
            }

            // 切换Active状态确认对话
            pendingSetActiveBangumiId?.let { bangumi ->
                val confirmationTitle = stringResource(
                    if (bangumi.isActive) {
                        R.string.item_confirm_hide
                    } else {
                        R.string.item_confirm_unhide
                    },
                )
                AlertDialog(
                    onDismissRequest = {
                        pendingSetActiveBangumiId = null
                    },
                    title = {
                        Text(confirmationTitle)
                    },
                    text = {
                        Text(bangumi.titleStr)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                pendingSetActiveBangumiId = null
                                onSetBangumiActiveClick(bangumi.bangumiIdInt)
                            },
                        ) {
                            Text(stringResource(R.string.action_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                pendingSetActiveBangumiId = null
                            },
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    },
                )
            }

            // 删除确认对话
            pendingDeleteBangumiId?.let { bangumi ->
                AlertDialog(
                    onDismissRequest = {
                        pendingDeleteBangumiId = null
                    },
                    title = {
                        Text(stringResource(R.string.item_confirm_delete))
                    },
                    text = {
                        Text(bangumi.titleStr)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                pendingDeleteBangumiId = null
                                onDeleteBangumiClick(bangumi.bangumiIdInt)
                            },
                        ) {
                            Text(stringResource(R.string.action_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                pendingDeleteBangumiId = null
                            },
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }
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

    isMoreMenuExpanded: Boolean,
    onMoreClick: () -> Unit,
    onDismissMoreMenu: () -> Unit,
    onMinus1Click: () -> Unit,
    isActive: Boolean,
    onSetActiveClick: () -> Unit,
    onDeleteClick: () -> Unit,

    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(
        if (isActive) uiState.themeColorLong else INACTIVE_COLOR_LONG
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorScheme.indexBorder),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.indexCardContainer,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = uiState.titleStr,
                    color = colorScheme.indexPrimaryContent,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = uiState.watchProgress.displayText(),
                    color = colorScheme.indexSecondaryContent,
                    style = MaterialTheme.typography.labelSmall,
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
                val buttonBorderColor = colorScheme.indexBorder
                val buttonColors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = colorScheme.indexButtonContainer,
                    contentColor = colorScheme.indexPrimaryContent,
                )

                CircleActionButton(
                    onClick = onAdd1Click,
                    borderColor = buttonBorderColor,
                    colors = buttonColors,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.index_item_button_plus_1),
                        contentDescription = stringResource(
                            R.string.index_watched_episode_increment,
                        ),
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
                        contentDescription = stringResource(R.string.action_edit),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    CircleActionButton(
                        onClick = onMoreClick,
                        borderColor = buttonBorderColor,
                        colors = buttonColors,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.action_more),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isMoreMenuExpanded,
                        onDismissRequest = onDismissMoreMenu,
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.index_watched_episode_decrement))
                            },
                            onClick = onMinus1Click,
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        if (isActive) {
                                            R.string.action_hide
                                        } else {
                                            R.string.action_unhide
                                        },
                                    ),
                                )
                            },
                            onClick = onSetActiveClick,
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete)) },
                            onClick = onDeleteClick,
                        )
                    }
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
        modifier = modifier.size(30.dp),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor,
        ),
        colors = colors,
        shape = CircleShape,
        content = content,
    )
}



@Preview
@Composable
private fun PreviewHere() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        IndexScreenContent(
            uiState = IndexUiState(
                bangumiList = List(10) { index ->
                    BangumiIndexItemUiState(
                        titleStr = "Bangumi Title ${index + 1}".repeat(index + 1),
                        watchProgress = BangumiWatchProgressUiState(
                            dayOfWeek = 1,
                            latestWatchedEpisode = 10,
                            latestAiredEpisode = 12,
                            totalEpisodes = 12,
                            startDate = LocalDate.of(2026, 7, 1),
                        ),
                        themeColorLong = listOf(
                            SettingsRepository.DEFAULT_01_COLOR_LONG,
                            SettingsRepository.DEFAULT_04_COLOR_LONG,
                            SettingsRepository.DEFAULT_07_COLOR_LONG,
                            SettingsRepository.DEFAULT_10_COLOR_LONG,
                        )[index.rem(4)],
                        bangumiIdInt = index + 1,
                        isActive = true,
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
            onEditClick = {},
            onMinus1BangumiClick = {},
            onSetBangumiActiveClick = {},
            onDeleteBangumiClick = {},
            onFilterStatusChanged = {},
            onDismissFilterSheet = {},
            onDismissDetailDialog = {},
            onTopAddClick = {},
            onTopSettingsClick = {},
        )
    }
}
