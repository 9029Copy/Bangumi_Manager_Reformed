package com.copy9029.bangumimanagerreformed.ui.calendar

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material.icons.filled.MoreVert
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.AddSheetViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheet
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.util.generateBangumiColorScheme
import kotlinx.coroutines.launch

@Composable
fun PageCalendarScreen(
    calendarViewModel: CalendarViewModel,
    addSheetViewModel: AddSheetViewModel,
    onBatchClick: () -> Unit,
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
                                    index = (uiState.todayWeekIndex() - WEEKS_PREFIX)
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

                            // TODO: 在此处添加更多日历菜单More选项。
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
                            CalendarSelectedDateDetailsRow(
                                // TODO
                            )
                        }
                    }
                }
            }
        }
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
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(
                                            index = (selectedWeekIndex - WEEKS_PREFIX)
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
            .padding(horizontal = 6.dp),
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
            .padding(horizontal = 6.dp),
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
private fun CalendarSelectedDateDetailsRow(
    // TODO
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        // TODO: 在此处添加所选日期全部事件的详细信息。
    }
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
                .padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
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
        color = colorScheme.cardContainer,
        shape = RoundedCornerShape(3.dp),
    ) {
        Text(
            text = item.title,
            modifier = Modifier.padding(horizontal = 1.dp, vertical = 2.dp),
            color = colorScheme.primaryContent,
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
                bangumisByDate = (0..8).associate { index ->
                    val date = today.plusDays(index.toLong() - 1L)
                    date to listOf(
                        CalendarBangumiItemUiState(
                            bangumiId = index + 1,
                            episodeId = index + 1,
                            title = "示例动画动画动画",
                            themeColorLong = 0xFF80FFFFL,
                        )
                    )
                },
                selectedDateEpochDay = today.toEpochDay() + 2,
            ),
            onAddClick = {},
            onDateClick = {},
        )
    }
}

