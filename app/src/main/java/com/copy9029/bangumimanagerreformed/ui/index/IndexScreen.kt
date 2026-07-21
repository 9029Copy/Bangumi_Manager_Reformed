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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiDetailDialog
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.AddSheetViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheet
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.util.generateBangumiColorScheme
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
    addSheetViewModel: AddSheetViewModel,
    onEditClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by indexViewModel.uiState.collectAsStateWithLifecycle()

    var isAddSheetVisible by rememberSaveable {
        mutableStateOf(false)
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
        onEditClick = onEditClick,
        onMinus1BangumiClick = indexViewModel::onMinus1BangumiClick,
        onSetBangumiActiveClick = indexViewModel::onSetBangumiActiveClick,
        onDeleteBangumiClick = indexViewModel::onDeleteBangumiClick,
        onFilterStatusChanged = indexViewModel::onFilterStatusChanged,
        onDismissFilterSheet = indexViewModel::onDismissFilterSheet,
        onDismissDetailDialog = indexViewModel::onDismissDetailDialog,
        onTopAddClick = {
            isAddSheetVisible = true
        },
        onTopMoreClick = indexViewModel::onTopMoreClick,
    )

    if (isAddSheetVisible) {
        BangumiAddSheet(
            viewModel = addSheetViewModel,
            defaultFirstBroadcastDate = LocalDate.now(),
            onDismissRequest = {
                isAddSheetVisible = false
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
    onTopMoreClick: () -> Unit,
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
                actions = {
                    IconButton(
                        onClick = onTopAddClick
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add bangumis",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    IconButton(
                        onClick = onTopMoreClick
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More (Screen)",
                            modifier = Modifier.size(32.dp),
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

            // 筛选表
            if (uiState.isFilterSheetVisible) {
                FilterBottomSheet(
                    status = uiState.sortAndFilterStatus,
                    onStatusChange = onFilterStatusChanged,
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
                        onDismissDetailDialog()
                        onEditClick(detail.inProjectIDInt)
                    },
                )
            }

            // 切换Active状态确认对话
            pendingSetActiveBangumiId?.let { bangumi ->
                val str1 = if (bangumi.isActive) "隐藏" else "取消隐藏"
                AlertDialog(
                    onDismissRequest = {
                        pendingSetActiveBangumiId = null
                    },
                    title = {
                        Text("你确定要${str1}这个项目吗？")
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
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                pendingSetActiveBangumiId = null
                            },
                        ) {
                            Text("取消")
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
                        Text("你确定要删除这个项目吗？")
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
                            Text("删除")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                pendingDeleteBangumiId = null
                            },
                        ) {
                            Text("取消")
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
    val colorScheme = generateBangumiColorScheme(   // TODO: theme Color
        if (isActive) uiState.themeColorLong else 0xFFFFFFFF
    )

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
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = uiState.titleStr,
                    color = colorScheme.primaryContent,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = uiState.watchProgressStr,
                    color = colorScheme.secondaryContent,
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

                Box {
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

                    DropdownMenu(
                        expanded = isMoreMenuExpanded,
                        onDismissRequest = onDismissMoreMenu,
                    ) {
                        DropdownMenuItem(
                            text = { Text("-1") },
                            onClick = onMinus1Click,
                        )

                        DropdownMenuItem(
                            text = { Text(if (isActive) "隐藏" else "取消隐藏") },
                            onClick = onSetActiveClick,
                        )

                        DropdownMenuItem(
                            text = { Text("删除") },
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



@Preview
@Composable
private fun PreviewHere() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        IndexScreenContent(
            uiState = IndexUiState(
                bangumiList = List(10) { index ->
                    BangumiIndexItemUiState(
                        titleStr = "Bangumi Title ${index + 1}".repeat(index + 1),
                        watchProgressStr = "周一 丨 已看完第 10 话 丨 更新到第 12 话",
                        themeColorLong = listOf(0xFFFFFFFF, 0xFFFF0000, 0xFF0000FF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF)[index.rem(6)],
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
            onTopMoreClick = {},
        )
    }
}


/*
// TODO: issues

1. 添加项目Sheet，开播日期太窄看不全
2. 筛选页面，按钮内文字出现换行

 */