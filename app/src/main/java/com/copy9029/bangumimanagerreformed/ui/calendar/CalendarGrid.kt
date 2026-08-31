package com.copy9029.bangumimanagerreformed.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.INACTIVE_COLOR_LONG
import com.copy9029.bangumimanagerreformed.ui.theme.generateBangumiColorScheme
import java.time.LocalDate

@Composable
internal fun DaysOfWeekHeader(
    modifier: Modifier = Modifier,
) {
    val weekdayLabels = stringArrayResource(R.array.weekday_labels_monday_first)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 2.dp),
    ) {
        weekdayLabels.forEach { day ->
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
internal fun CalendarWeekRow(
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
private fun CalendarDateCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    bangumis: List<CalendarBangumiItemUiState>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthNames = stringArrayResource(R.array.calendar_month_names)

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
                .padding(1.dp),
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
                            .size(30.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.calendar_today_short),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else if (date.dayOfMonth == 1) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Spacer(modifier = Modifier.width(5.dp))

                        Box(
                            modifier = Modifier
                                .height(22.dp)
                                .width(5.dp)
                                .background(color = MaterialTheme.colorScheme.primary)
                        )

                        Spacer(modifier = Modifier.width(3.dp))

                        Text(
                            text = monthNames[date.monthValue - 1],
                            color = MaterialTheme.colorScheme.primary,
                            style = if (date.monthValue < 11){
                                MaterialTheme.typography.labelLarge
                            } else {
                                MaterialTheme.typography.labelMedium
                            },
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
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
    val colorScheme = generateBangumiColorScheme(
        if (item.isActive) item.themeColorLong else INACTIVE_COLOR_LONG
    )

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
            text = if (item.isDone) {
                stringResource(R.string.calendar_completed_item_title, item.title)
            } else {
                item.title
            },
            modifier = Modifier.padding(horizontal = 1.dp, vertical = 1.dp),
            color = Color.White,
            fontSize = 8.sp,
            lineHeight = 16.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
        )
    }
}


