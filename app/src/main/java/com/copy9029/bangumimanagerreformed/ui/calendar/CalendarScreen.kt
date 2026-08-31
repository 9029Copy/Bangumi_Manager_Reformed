package com.copy9029.bangumimanagerreformed.ui.calendar

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiDetailDialog
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheet
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheetViewModel
import com.copy9029.bangumimanagerreformed.ui.components.AppDatePickerDialog
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel,
    bangumiAddSheetViewModel: BangumiAddSheetViewModel,
    onBatchClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    var addSheetDefaultDateEpochDay by rememberSaveable {
        mutableStateOf<Long?>(null)
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

    CalendarScreenContent(
        uiState = uiState,
        onAddClick = { defaultDate ->
            addSheetDefaultDateEpochDay = defaultDate.toEpochDay()
        },
        onDateClick = calendarViewModel::onDateClick,
        onDateSelected = calendarViewModel::onDateSelected,
        onBangumiClick = calendarViewModel::onBangumiClick,
        onDismissDetailDialog = calendarViewModel::onDismissDetailDialog,
        onMarkEpisodeDoneClick = calendarViewModel::onMarkEpisodeDoneClick,
        onMarkEpisodeUndoneClick = calendarViewModel::onMarkEpisodeUndoneClick,
        onToggleBangumiActiveClick = calendarViewModel::onToggleBangumiActiveClick,
        onDeleteBangumiClick = calendarViewModel::onDeleteBangumiClick,
        onEditClick = { bangumiId ->
            pendingEditBangumiId = bangumiId
            calendarViewModel.onDismissDetailDialog()
        },
        onSettingsClick = onSettingsClick,
        modifier = modifier,
    )

    addSheetDefaultDateEpochDay?.let { defaultDateEpochDay ->
        BangumiAddSheet(
            viewModel = bangumiAddSheetViewModel,
            defaultFirstBroadcastDate = LocalDate.ofEpochDay(defaultDateEpochDay),
            onDismissRequest = {
                addSheetDefaultDateEpochDay = null
            },
            onBatchClick = {
                addSheetDefaultDateEpochDay = null
                onBatchClick()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScreenContent(
    uiState: CalendarUiState,
    onAddClick: (LocalDate) -> Unit,
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
    val dateOutOfRangeMessage = stringResource(R.string.calendar_date_out_of_range)
    val listState = rememberSaveable(
        uiState.firstWeekStart.toEpochDay(),
        uiState.weekCount,
        uiState.initialWeekIndex,
        saver = LazyListState.Saver,
    ) {
        LazyListState(
            firstVisibleItemIndex = uiState.initialWeekIndex,
        )
    }
    val coroutineScope = rememberCoroutineScope()
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
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable(
                                onClickLabel = stringResource(R.string.calendar_jump_to_date),
                                role = Role.Button,
                                onClick = {
                                    isDatePickerVisible = true
                                },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.calendar_year_month,
                                displayedMonth.year,
                                displayedMonth.monthValue,
                            ),
                            fontSize = 20.sp,
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.scrollToItem(
                                    index = (uiState.todayWeekIndex() - uiState.weeksPrefix)
                                        .coerceAtLeast(0),
                                )
                            }
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.calendar_today_short),
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (uiState.selectedDateEpochDay == null) {
                                onAddClick(LocalDate.now())
                            } else {
                                onAddClick(LocalDate.ofEpochDay(uiState.selectedDateEpochDay))
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.action_add_item),
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                                onAddClick = {
                                    onAddClick(selectedDate)
                                },
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
                onEditClick(detail.inProjectIDInt)
            },
        )
    }

    if (isDatePickerVisible) {
        AppDatePickerDialog(
            initialDate = LocalDate.now(),
            onDateSelected = { selectedDate ->
                val selectedWeekIndex = uiState.weekIndexFor(selectedDate)

                if (selectedWeekIndex == null) {
                    Toast.makeText(
                        context,
                        dateOutOfRangeMessage,
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
            },
            onDismissRequest = {
                isDatePickerVisible = false
            },
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
                initialWeekIndex = 10 - 1 + 17,
                bangumisByDate = (0..20).associate { index ->
                    val date = today.plusDays(index.toLong() - 1L)
                    date to listOf(
                        CalendarBangumiItemUiState(
                            bangumiId = index + 1,
                            episodeId = index + 1,
                            title = "示例示例示例示例示例示例示例示例示例示例示例示例示例示例示例",
                            themeColorLong = listOf(
                                SettingsRepository.DEFAULT_01_COLOR_LONG,
                                SettingsRepository.DEFAULT_04_COLOR_LONG,
                                SettingsRepository.DEFAULT_07_COLOR_LONG,
                                SettingsRepository.DEFAULT_10_COLOR_LONG,
                            )[index.rem(4)],
                            isDone = false,
                        ),
                        CalendarBangumiItemUiState(
                            bangumiId = index + 114,
                            episodeId = index + 1,
                            title = "示例示例示例示例示例示例示例示例示例示例示例示例示例示例示例",
                            themeColorLong = listOf(
                                SettingsRepository.DEFAULT_01_COLOR_LONG,
                                SettingsRepository.DEFAULT_04_COLOR_LONG,
                                SettingsRepository.DEFAULT_07_COLOR_LONG,
                                SettingsRepository.DEFAULT_10_COLOR_LONG,
                            )[index.rem(4)],
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
