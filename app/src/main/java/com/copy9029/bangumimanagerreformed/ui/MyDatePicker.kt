package com.copy9029.bangumimanagerreformed.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
fun MyDatePickerDialog(
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
            Text("选择日期")
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
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun MonthNavigationRow(
    month: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${month.year} 年 ${month.monthValue} 月",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onPreviousMonthClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = "上个月",
            )
        }
        IconButton(onClick = onNextMonthClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "下个月",
            )
        }
    }
}

@Composable
private fun WeekdayHeader(
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        mondayFirstWeekdayLabels.forEach { label ->
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

private val mondayFirstWeekdayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
private val monthOffsetOrigin: YearMonth = YearMonth.of(1970, 1)
private const val DaysPerWeek = 7
private const val CalendarRowCount = 6

@Preview(showBackground = true)
@Composable
private fun PreviewMyDatePickerDialog() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        MyDatePickerDialog(
            initialDate = LocalDate.of(2026, 8, 4),
            onDateSelected = {},
            onDismissRequest = {},
        )
    }
}
