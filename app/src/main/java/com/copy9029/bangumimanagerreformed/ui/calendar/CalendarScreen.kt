package com.copy9029.bangumimanagerreformed.ui.calendar

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.YearMonth
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.themeColorByMonth
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiDetailDialog
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.AddSheetViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheet
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.util.generateBangumiColorScheme
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel,
    addSheetViewModel: AddSheetViewModel,
    onBatchClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    var isAddSheetVisible by rememberSaveable {
        mutableStateOf(false)
    }

    CalendarScreenContent(
        uiState = uiState,
        onAddClick = {
            isAddSheetVisible = true
        },
        onDateClick = calendarViewModel::onDateClick,
        onDateSelected = calendarViewModel::onDateSelected,
        onBangumiClick = calendarViewModel::onBangumiClick,
        onDismissDetailDialog = calendarViewModel::onDismissDetailDialog,
        onMarkEpisodeDoneClick = calendarViewModel::onMarkEpisodeDoneClick,
        onMarkEpisodeUndoneClick = calendarViewModel::onMarkEpisodeUndoneClick,
        onToggleBangumiActiveClick = calendarViewModel::onToggleBangumiActiveClick,
        onDeleteBangumiClick = calendarViewModel::onDeleteBangumiClick,
        onEditClick = onEditClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
    )

    if (isAddSheetVisible) {
        BangumiAddSheet(
            viewModel = addSheetViewModel,
            defaultFirstBroadcastDate = if (uiState.selectedDateEpochDay == null) {
                LocalDate.now()
            } else {
                LocalDate.ofEpochDay(uiState.selectedDateEpochDay!!)
            },
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
private fun CalendarScreenContent(
    uiState: CalendarUiState,
    onAddClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onBangumiClick: (Int) -> Unit,
    onDismissDetailDialog: () -> Unit,
    onMarkEpisodeDoneClick: (Int, Int) -> Unit,
    onMarkEpisodeUndoneClick: (Int, Int) -> Unit,
    onToggleBangumiActiveClick: (Int) -> Unit,
    onDeleteBangumiClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = uiState.initialWeekIndex,
    )
    val coroutineScope = rememberCoroutineScope()
    var isMoreMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var isDatePickerVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val displayedMonth by remember(uiState.firstWeekStart, listState) {
        derivedStateOf {
            YearMonth.from(
                uiState.firstWeekStart
                    .plusWeeks(listState.firstVisibleItemIndex.toLong())
                    .plusDays(3),
            )
        }
    }

    LaunchedEffect(uiState.firstWeekStart, uiState.initialWeekIndex) {
        listState.scrollToItem(uiState.initialWeekIndex)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("${displayedMonth.year} 年 ${displayedMonth.monthValue} 月")
                },
                actions = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(
                                    index = (uiState.todayWeekIndex() - uiState.weeksPrefix)
                                        .coerceAtLeast(0),
                                )
                            }
                        },
                    ) {
                        Text(
                            text = "今",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    IconButton(onClick = onAddClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "添加项目",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Box {
                        IconButton(
                            onClick = {
                                isMoreMenuExpanded = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "更多",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }

                        DropdownMenu(
                            expanded = isMoreMenuExpanded,
                            onDismissRequest = {
                                isMoreMenuExpanded = false
                            },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("跳转到日期")
                                },
                                onClick = {
                                    isMoreMenuExpanded = false
                                    isDatePickerVisible = true
                                },
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("设置")
                                },
                                onClick = {
                                    isMoreMenuExpanded = false
                                    onSettingsClick()
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            DaysOfWeekHeader()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                items(
                    count = uiState.weekCount,
                    key = { weekIndex ->
                        uiState.firstWeekStart
                            .plusWeeks(weekIndex.toLong())
                            .toEpochDay()
                    },
                ) { weekIndex ->
                    val weekStart = uiState.firstWeekStart
                        .plusWeeks(weekIndex.toLong())

                    Column(modifier = Modifier.fillMaxWidth()) {
                        CalendarWeekRow(
                            weekStart = weekStart,
                            bangumisByDate = uiState.bangumisByDate,
                            isDateSelected = uiState::isDateSelected,
                            onDateClick = onDateClick,
                        )

                        if (uiState.isWeekSelected(weekStart)) {
                            val selectedDate = LocalDate.ofEpochDay(
                                requireNotNull(uiState.selectedDateEpochDay)
                            )
                            CalendarSelectedDateDetails(
                                bangumis = uiState.bangumisByDate[selectedDate].orEmpty(),
                                onAddClick = onAddClick,
                                onBangumiClick = onBangumiClick,
                                onMarkEpisodeDoneClick = onMarkEpisodeDoneClick,
                                onMarkEpisodeUndoneClick = onMarkEpisodeUndoneClick,
                                onToggleBangumiActiveClick = onToggleBangumiActiveClick,
                                onDeleteBangumiClick = onDeleteBangumiClick,
                                onEditClick = onEditClick,
                            )
                        }
                    }
                }
            }
        }
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

    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.now()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
        )

        DatePickerDialog(
            onDismissRequest = {
                isDatePickerVisible = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.let { millis ->
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                            }
                            ?.let { selectedDate ->
                                val selectedWeekIndex = uiState.weekIndexFor(
                                    selectedDate
                                )

                                if (selectedWeekIndex == null) {
                                    Toast.makeText(
                                        context,
                                        "日期超出可显示范围",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                } else {
                                    onDateSelected(selectedDate)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(
                                            index = (selectedWeekIndex - uiState.weeksPrefix)
                                                .coerceAtLeast(0),
                                        )
                                    }
                                }
                            }

                        isDatePickerVisible = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isDatePickerVisible = false
                    },
                ) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DaysOfWeekHeader(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 2.dp),
    ) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun CalendarWeekRow(
    weekStart: LocalDate,
    bangumisByDate: Map<LocalDate, List<CalendarBangumiItemUiState>>,
    isDateSelected: (LocalDate) -> Boolean,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .height(IntrinsicSize.Min)
            .padding(horizontal = 2.dp),
    ) {
        repeat(7) { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            CalendarDateCell(
                date = date,
                isToday = date == LocalDate.now(),
                isSelected = isDateSelected(date),
                bangumis = bangumisByDate[date].orEmpty(),
                onClick = {
                    onDateClick(date)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun CalendarSelectedDateDetails(
    bangumis: List<CalendarBangumiItemUiState>,
    onAddClick: () -> Unit,
    onBangumiClick: (Int) -> Unit,
    onMarkEpisodeDoneClick: (Int, Int) -> Unit,
    onMarkEpisodeUndoneClick: (Int, Int) -> Unit,
    onToggleBangumiActiveClick: (Int) -> Unit,
    onDeleteBangumiClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedMoreMenuBangumiId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    var pendingSetActiveBangumi by remember {
        mutableStateOf<CalendarBangumiItemUiState?>(null)
    }
    var pendingDeleteBangumi by remember {
        mutableStateOf<CalendarBangumiItemUiState?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        bangumis.forEach { bangumi ->
            CalendarSelectedDateDetailCard(
                item = bangumi,
                isMoreMenuExpanded = expandedMoreMenuBangumiId == bangumi.bangumiId,
                onClick = {
                    onBangumiClick(bangumi.bangumiId)
                },
                onDoneButtonClick = {
                    if (bangumi.isDone) {
                        onMarkEpisodeUndoneClick(bangumi.bangumiId, bangumi.episodeId)
                    } else {
                        onMarkEpisodeDoneClick(bangumi.bangumiId, bangumi.episodeId)
                    }
                },
                onEditClick = {
                    onEditClick(bangumi.bangumiId)
                },
                onMoreClick = {
                    expandedMoreMenuBangumiId = bangumi.bangumiId
                },
                onDismissMoreMenu = {
                    expandedMoreMenuBangumiId = null
                },
                onToggleActiveClick = {
                    expandedMoreMenuBangumiId = null
                    pendingSetActiveBangumi = bangumi
                },
                onDeleteClick = {
                    expandedMoreMenuBangumiId = null
                    pendingDeleteBangumi = bangumi
                },
            )
        }

        Card(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 30.dp),
            shape = RoundedCornerShape(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "添加项目",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    pendingSetActiveBangumi?.let { bangumi ->
        val actionText = if (bangumi.isActive) "隐藏" else "取消隐藏"
        AlertDialog(
            onDismissRequest = {
                pendingSetActiveBangumi = null
            },
            title = {
                Text("你确定要${actionText}这个项目吗？")
            },
            text = {
                Text(bangumi.title)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingSetActiveBangumi = null
                        onToggleBangumiActiveClick(bangumi.bangumiId)
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingSetActiveBangumi = null
                    },
                ) {
                    Text("取消")
                }
            },
        )
    }

    pendingDeleteBangumi?.let { bangumi ->
        AlertDialog(
            onDismissRequest = {
                pendingDeleteBangumi = null
            },
            title = {
                Text("你确定要删除这个项目吗？")
            },
            text = {
                Text(bangumi.title)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteBangumi = null
                        onDeleteBangumiClick(bangumi.bangumiId)
                    },
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingDeleteBangumi = null
                    },
                ) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun CalendarSelectedDateDetailCard(
    item: CalendarBangumiItemUiState,
    isMoreMenuExpanded: Boolean,
    onClick: () -> Unit,
    onDoneButtonClick: () -> Unit,
    onEditClick: () -> Unit,
    onMoreClick: () -> Unit,
    onDismissMoreMenu: () -> Unit,
    onToggleActiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(item.themeColorLong)
    val titleColor = if (item.isDone) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val episodeColor = if (item.isDone) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = onDoneButtonClick,
                ) {
                    if (item.isDone) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "已完成",
                            modifier = Modifier.size(24.dp),
                            tint = colorScheme.main,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.calendar_outline_circle),
                            contentDescription = "未完成",
                            modifier = Modifier.size(22.dp),
                            tint = colorScheme.main,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = item.title,
                        color = titleColor,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = "第 ${item.episodeId} 集",
                        color = episodeColor,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalendarCircleActionButton(
                    onClick = onEditClick,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(18.dp),
                    )
                }

                Box {
                    CalendarCircleActionButton(
                        onClick = onMoreClick,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "更多",
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    DropdownMenu(
                        expanded = isMoreMenuExpanded,
                        onDismissRequest = onDismissMoreMenu,
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (item.isActive) "隐藏" else "取消隐藏") },
                            onClick = onToggleActiveClick,
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
private fun CalendarCircleActionButton(
    onClick: () -> Unit,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
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

@Composable
private fun CalendarDateCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    bangumis: List<CalendarBangumiItemUiState>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .padding(1.dp),
        shape = RoundedCornerShape(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f),
                contentAlignment = Alignment.Center,
            ) {
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "今",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Text(
                        text = if (date.dayOfMonth == 1) {
                            date.monthValue.toChineseMonthText()
                        } else {
                            date.dayOfMonth.toString()
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (date.dayOfMonth == 1) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            }

            bangumis.forEach { item ->
                CalendarBangumiTag(item)
            }
        }
    }
}

@Composable
private fun CalendarBangumiTag(
    item: CalendarBangumiItemUiState,
    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(item.themeColorLong)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (item.isDone) {
            colorScheme.calendarFinishedTagContainer
        } else {
            colorScheme.calendarUnfinishedTagContainer
        },
        shape = RoundedCornerShape(3.dp),
    ) {
        Text(
            text = if (item.isDone) "✔${item.title}" else item.title,
            modifier = Modifier.padding(horizontal = 1.dp, vertical = 2.dp),
            color = Color.White,
            fontSize = 8.sp,
            lineHeight = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
        )
    }
}


private fun CalendarUiState.todayWeekIndex(
    today: LocalDate = LocalDate.now(),
): Int {
    val currentWeekStart = today.minusDays(
        (today.dayOfWeek.value - 1).toLong()
    )

    return ChronoUnit.WEEKS.between(
        firstWeekStart,
        currentWeekStart,
    ).toInt().coerceIn(0, weekCount - 1)
}

private fun CalendarUiState.weekIndexFor(date: LocalDate): Int? {
    val weekStart = date.minusDays(
        (date.dayOfWeek.value - 1).toLong()
    )
    val weekIndex = ChronoUnit.WEEKS.between(
        firstWeekStart,
        weekStart,
    )

    return weekIndex
        .takeIf { it in 0L until weekCount.toLong() }
        ?.toInt()
}

private fun Int.toChineseMonthText(): String {
    return when (this) {
        1 -> "一月"
        2 -> "二月"
        3 -> "三月"
        4 -> "四月"
        5 -> "五月"
        6 -> "六月"
        7 -> "七月"
        8 -> "八月"
        9 -> "九月"
        10 -> "十月"
        11 -> "十一月"
        12 -> "十二月"
        else -> error("Invalid month value: $this")
    }
}






@Preview(showBackground = true)
@Composable
private fun PreviewCalendarScreenContent() {
    val today = LocalDate.now()
    val weeksBefore = 10
    BangumiManagerReformedTheme(dynamicColor = false) {
        CalendarScreenContent(
            uiState = CalendarUiState(
                firstWeekStart = today.minusDays(
                    (today.dayOfWeek.value - 1 + 7 * 10).toLong()
                ),
                weekCount = 21,
                initialWeekIndex = 10 - 1,
                bangumisByDate = (0..20).associate { index ->
                    val date = today.plusDays(index.toLong() - 1L)
                    date to listOf(
                        CalendarBangumiItemUiState(
                            bangumiId = index + 1,
                            episodeId = index + 1,
                            title = "示例示例示例示例示例示例示例示例示例示例示例示例示例示例示例",
                            themeColorLong = themeColorByMonth[listOf(1,4,7,10)[index.rem(4)]]!!,
                            isDone = false,
                        ),
                        CalendarBangumiItemUiState(
                            bangumiId = index + 114,
                            episodeId = index + 1,
                            title = "示例示例示例示例示例示例示例示例示例示例示例示例示例示例示例",
                            themeColorLong = themeColorByMonth[listOf(1,4,7,10)[index.rem(4)]]!!,
                            isDone = true,
                        ),
                    )
                },
                selectedDateEpochDay = today.toEpochDay() + 2,
            ),
            onAddClick = {},
            onDateClick = {},
            onDateSelected = {},
            onBangumiClick = {},
            onDismissDetailDialog = {},
            onMarkEpisodeDoneClick = { _, _ -> },
            onMarkEpisodeUndoneClick = { _, _ -> },
            onToggleBangumiActiveClick = {},
            onDeleteBangumiClick = {},
            onEditClick = {},
            onSettingsClick = {},
        )
    }
}
