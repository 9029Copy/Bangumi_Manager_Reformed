package com.copy9029.bangumimanagerreformed.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.components.wheel_picker.CurrentIndex
import com.copy9029.bangumimanagerreformed.ui.components.wheel_picker.FVerticalWheelPicker
import com.copy9029.bangumimanagerreformed.ui.components.wheel_picker.rememberFWheelPickerState
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * A Monday-first date picker adapted for this project.
 *
 * Original implementation by re-ovo:
 * https://gist.github.com/re-ovo/2b4cc2c4fdfb03784fa8643dd360f4a5
 */
@Composable
fun AppDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialEpochDay = initialDate.toEpochDay()
    var selectedDateEpochDay by rememberSaveable(initialEpochDay) {
        mutableLongStateOf(initialEpochDay)
    }
    var displayedMonthOffset by rememberSaveable(initialEpochDay) {
        mutableLongStateOf(YearMonth.from(initialDate).toMonthOffset())
    }
    var isYearMonthPickerVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val displayedMonth = remember(displayedMonthOffset) {
        monthOffsetOrigin.plusMonths(displayedMonthOffset)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = null,
            )
        },
        title = {
            Text(stringResource(R.string.date_picker_title))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MonthNavigationRow(
                    month = displayedMonth,
                    onPreviousMonthClick = {
                        displayedMonthOffset--
                    },
                    onNextMonthClick = {
                        displayedMonthOffset++
                    },
                    onYearMonthClick = {
                        isYearMonthPickerVisible = true
                    },
                )
                WeekdayHeader()
                MonthGrid(
                    month = displayedMonth,
                    selectedDate = LocalDate.ofEpochDay(selectedDateEpochDay),
                    onDateSelected = { date ->
                        selectedDateEpochDay = date.toEpochDay()
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(LocalDate.ofEpochDay(selectedDateEpochDay))
                    onDismissRequest()
                },
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )

    if (isYearMonthPickerVisible) {
        YearMonthPickerDialog(
            initialMonth = displayedMonth,
            onMonthSelected = { month ->
                displayedMonthOffset = month.toMonthOffset()
                isYearMonthPickerVisible = false
            },
            onDismissRequest = {
                isYearMonthPickerVisible = false
            },
        )
    }
}

@Composable
private fun MonthNavigationRow(
    month: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onYearMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectYearMonthLabel = stringResource(R.string.date_picker_select_year_month)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .clickable(
                    onClickLabel = selectYearMonthLabel,
                    onClick = onYearMonthClick,
                )
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.date_picker_year_month,
                    month.year,
                    month.monthValue,
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
            )
        }
        IconButton(onClick = onPreviousMonthClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.date_picker_previous_month),
            )
        }
        IconButton(onClick = onNextMonthClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = stringResource(R.string.date_picker_next_month),
            )
        }
    }
}

@Composable
private fun YearMonthPickerDialog(
    initialMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val initialYear = initialMonth.year.coerceIn(MinimumPickerYear, MaximumPickerYear)
    var selectedYear by rememberSaveable(initialMonth) {
        mutableIntStateOf(initialYear)
    }
    var selectedMonth by rememberSaveable(initialMonth) {
        mutableIntStateOf(initialMonth.monthValue)
    }
    val yearPickerState = rememberFWheelPickerState(
        initialIndex = initialYear - MinimumPickerYear,
    )
    val monthPickerState = rememberFWheelPickerState(
        initialIndex = initialMonth.monthValue - 1,
    )

    yearPickerState.CurrentIndex { index ->
        if (index in 0 until YearPickerItemCount) {
            selectedYear = MinimumPickerYear + index
        }
    }
    monthPickerState.CurrentIndex { index ->
        if (index in 0 until MonthsPerYear) {
            selectedMonth = index + 1
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.date_picker_select_year_month),
                    style = MaterialTheme.typography.titleMedium,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(YearMonthPickerHeight),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FVerticalWheelPicker(
                        modifier = Modifier.weight(1f),
                        count = YearPickerItemCount,
                        state = yearPickerState,
                        key = { index -> MinimumPickerYear + index },
                        itemHeight = WheelPickerItemHeight,
                        unfocusedCount = WheelPickerUnfocusedCount,
                    ) { index ->
                        Text(
                            text = stringResource(
                                R.string.date_picker_year,
                                MinimumPickerYear + index,
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    FVerticalWheelPicker(
                        modifier = Modifier.weight(1f),
                        count = MonthsPerYear,
                        state = monthPickerState,
                        key = { index -> index + 1 },
                        itemHeight = WheelPickerItemHeight,
                        unfocusedCount = WheelPickerUnfocusedCount,
                    ) { index ->
                        Text(
                            text = stringResource(R.string.date_picker_month, index + 1),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton(
                        onClick = {
                            val year = yearPickerState.currentIndexSnapshot
                                .takeIf { it in 0 until YearPickerItemCount }
                                ?.let { MinimumPickerYear + it }
                                ?: selectedYear
                            val month = monthPickerState.currentIndexSnapshot
                                .takeIf { it in 0 until MonthsPerYear }
                                ?.let { it + 1 }
                                ?: selectedMonth
                            onMonthSelected(YearMonth.of(year, month))
                        },
                    ) {
                        Text(stringResource(R.string.action_confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader(
    modifier: Modifier = Modifier,
) {
    val weekdayLabels = stringArrayResource(
        R.array.weekday_labels_monday_first,
    )

    Row(modifier = modifier.fillMaxWidth()) {
        weekdayLabels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDayColumn = month.atDay(1).dayOfWeek.value - 1
    val numberOfDays = month.lengthOfMonth()
    val today = LocalDate.now()

    Column(modifier = modifier.fillMaxWidth()) {
        repeat(CalendarRowCount) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(DaysPerWeek) { columnIndex ->
                    val cellIndex = rowIndex * DaysPerWeek + columnIndex
                    val dayOfMonth = cellIndex - firstDayColumn + 1
                    val date = if (dayOfMonth in 1..numberOfDays) {
                        month.atDay(dayOfMonth)
                    } else {
                        null
                    }

                    DayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        onClick = onDateSelected,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (date == null) return@Box

        val containerColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
        val contentColor = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val todayBorder = if (isToday && !isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (todayBorder == null) {
                        Modifier
                    } else {
                        Modifier.border(todayBorder, CircleShape)
                    },
                )
                .clip(CircleShape)
                .background(containerColor)
                .clickable {
                    onClick(date)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun YearMonth.toMonthOffset(): Long {
    return ChronoUnit.MONTHS.between(monthOffsetOrigin, this)
}

private val monthOffsetOrigin: YearMonth = YearMonth.of(1970, 1)
private const val DaysPerWeek = 7
private const val CalendarRowCount = 6
private const val MinimumPickerYear = 1970
private const val MaximumPickerYear = 4000
private const val YearPickerItemCount = MaximumPickerYear - MinimumPickerYear + 1
private const val MonthsPerYear = 12
private const val WheelPickerUnfocusedCount = 2
private val WheelPickerItemHeight = 40.dp
private val YearMonthPickerHeight =
    WheelPickerItemHeight * (WheelPickerUnfocusedCount * 2 + 1)

@Preview(showBackground = true)
@Composable
private fun PreviewAppDatePickerDialog() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        AppDatePickerDialog(
            initialDate = LocalDate.of(2026, 8, 4),
            onDateSelected = {},
            onDismissRequest = {},
        )
    }
}
