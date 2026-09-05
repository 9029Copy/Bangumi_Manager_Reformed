package com.copy9029.bangumimanagerreformed.ui.profile.color_settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiWatchProgressUiState
import com.copy9029.bangumimanagerreformed.ui.bangumi.displayText
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.ui.theme.generateBangumiColorScheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColorSettingsPreviewSection(
    modifier: Modifier = Modifier,
    default01ColorLong: Long,
    default04ColorLong: Long,
    default07ColorLong: Long,
    default10ColorLong: Long,
    initiallyExpanded: Boolean = false,
) {
    val monthNames = stringArrayResource(R.array.calendar_month_names)
    val settingsPreviewItems = listOf(
        ColorSettingsPreviewItem(
            month = 1,
            title = stringResource(
                R.string.profile_color_preview_item_title,
                monthNames[0],
            ),
            colorLong = default01ColorLong,
        ),
        ColorSettingsPreviewItem(
            month = 4,
            title = stringResource(
                R.string.profile_color_preview_item_title,
                monthNames[3],
            ),
            colorLong = default04ColorLong,
        ),
        ColorSettingsPreviewItem(
            month = 7,
            title = stringResource(
                R.string.profile_color_preview_item_title,
                monthNames[6],
            ),
            colorLong = default07ColorLong,
        ),
        ColorSettingsPreviewItem(
            month = 10,
            title = stringResource(
                R.string.profile_color_preview_item_title,
                monthNames[9],
            ),
            colorLong = default10ColorLong,
        ),
    )
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(1) }
    val selectedPreviewItem = settingsPreviewItems.firstOrNull { it.month == selectedMonth }
        ?: settingsPreviewItems.first()
    val selectedColorScheme = generateBangumiColorScheme(selectedPreviewItem.colorLong)
    val indexSubtitle = BangumiWatchProgressUiState(
        dayOfWeek = 1,
        latestWatchedEpisode = 1,
        latestAiredEpisode = 12,
        totalEpisodes = 12,
        startDate = LocalDate.of(2025, 1, 1),
    ).displayText()

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(
                start = 10.dp,
                top = 8.dp,
                end = 10.dp,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile_color_preview_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(
                    if (isExpanded) {
                        R.string.profile_color_preview_collapse
                    } else {
                        R.string.profile_color_preview_expand
                    },
                ),
                modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isExpanded) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                settingsPreviewItems.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = item.month == selectedMonth,
                        onClick = { selectedMonth = item.month },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = settingsPreviewItems.size,
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = selectedColorScheme.indexCardContainer,
                            activeContentColor = selectedColorScheme.indexPrimaryContent,
                            activeBorderColor = selectedColorScheme.indexBorder,
                            inactiveContainerColor = Color.Unspecified,
                        ),
                    ) {
                        Text(stringResource(R.string.profile_color_preview_month, item.month))
                    }
                }
            }

            ColorSettingsPreviewLabel(text = stringResource(R.string.profile_color_preview_index))
            ColorSettingsPreviewIndexItem(
                item = selectedPreviewItem,
                subtitle = indexSubtitle,
            )

            ColorSettingsPreviewLabel(text = stringResource(R.string.profile_color_preview_calendar))
            ColorSettingsPreviewCalendarWeekRow(
                item = selectedPreviewItem,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
            )
        }
    }
}

private data class ColorSettingsPreviewItem(
    val month: Int,
    val title: String,
    val colorLong: Long,
)

@Composable
private fun ColorSettingsPreviewLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(
            start = 16.dp,
            top = 10.dp,
            end = 16.dp,
            bottom = 2.dp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun ColorSettingsPreviewIndexItem(
    item: ColorSettingsPreviewItem,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(item.colorLong)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorScheme.indexBorder),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.indexCardContainer,
        ),
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
                    text = item.title,
                    color = colorScheme.indexPrimaryContent,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = colorScheme.indexSecondaryContent,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorSettingsPreviewIndexAction(
                    painter = painterResource(R.drawable.index_item_button_plus_1),
                    colorLong = item.colorLong,
                )
                ColorSettingsPreviewIndexAction(
                    painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Edit,
                    ),
                    colorLong = item.colorLong,
                )
                ColorSettingsPreviewIndexAction(
                    painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.MoreVert,
                    ),
                    colorLong = item.colorLong,
                )
            }
        }
    }
}

@Composable
private fun ColorSettingsPreviewIndexAction(
    painter: Painter,
    colorLong: Long,
    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(colorLong)

    Surface(
        modifier = modifier.size(30.dp),
        shape = CircleShape,
        color = colorScheme.indexButtonContainer,
        border = BorderStroke(1.dp, colorScheme.indexBorder),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colorScheme.indexPrimaryContent,
            )
        }
    }
}

@Composable
private fun ColorSettingsPreviewCalendarWeekRow(
    item: ColorSettingsPreviewItem,
    modifier: Modifier = Modifier,
) {
    val monthName = stringArrayResource(R.array.calendar_month_names)[item.month - 1]
    val calendarPreviewStates = listOf(
        ColorSettingsPreviewCalendarState(isDoneList = listOf(false), isSelected = false),
        ColorSettingsPreviewCalendarState(isDoneList = listOf(true), isSelected = false),
        ColorSettingsPreviewCalendarState(isDoneList = listOf(false), isSelected = true),
        ColorSettingsPreviewCalendarState(isDoneList = listOf(true), isSelected = true),
        ColorSettingsPreviewCalendarState(isDoneList = listOf(false, false), isSelected = false),
        ColorSettingsPreviewCalendarState(isDoneList = listOf(false, true), isSelected = false),
        ColorSettingsPreviewCalendarState(isDoneList = listOf(true, true), isSelected = false),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 2.dp, vertical = 2.dp),
    ) {
        repeat(7) { index ->
            val previewState = calendarPreviewStates[index]
            val dateLabel: ColorSettingsPreviewDateLabel = when (index) {
                0 -> ColorSettingsPreviewDateLabel.Today
                1 -> ColorSettingsPreviewDateLabel.Month(monthName)
                else -> ColorSettingsPreviewDateLabel.Day(index)
            }

            ColorSettingsPreviewCalendarDateCell(
                dateLabel = dateLabel,
                item = item,
                isDoneList = previewState.isDoneList,
                isSelected = previewState.isSelected,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

private data class ColorSettingsPreviewCalendarState(
    val isDoneList: List<Boolean>,
    val isSelected: Boolean,
)

private sealed interface ColorSettingsPreviewDateLabel {
    data object Today : ColorSettingsPreviewDateLabel
    data class Month(val text: String) : ColorSettingsPreviewDateLabel
    data class Day(val dayOfMonth: Int) : ColorSettingsPreviewDateLabel
}

@Composable
private fun ColorSettingsPreviewCalendarDateCell(
    dateLabel: ColorSettingsPreviewDateLabel,
    item: ColorSettingsPreviewItem,
    isDoneList: List<Boolean>,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(1.dp),
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
                when (dateLabel) {
                    ColorSettingsPreviewDateLabel.Today -> ColorSettingsPreviewTodayLabel()
                    is ColorSettingsPreviewDateLabel.Month -> ColorSettingsPreviewMonthLabel(dateLabel.text)
                    is ColorSettingsPreviewDateLabel.Day -> Text(
                        text = dateLabel.dayOfMonth.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            isDoneList.forEach { isDone ->
                ColorSettingsPreviewCalendarTag(
                    item = item,
                    isDone = isDone,
                )
            }
        }
    }
}

@Composable
private fun ColorSettingsPreviewTodayLabel() {
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
}

@Composable
private fun ColorSettingsPreviewMonthLabel(text: String) {
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
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ColorSettingsPreviewCalendarTag(
    item: ColorSettingsPreviewItem,
    isDone: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(item.colorLong)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isDone) {
            colorScheme.calendarFinishedTagContainer
        } else {
            colorScheme.calendarUnfinishedTagContainer
        },
        shape = RoundedCornerShape(3.dp),
    ) {
        Text(
            text = if (isDone) {
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

@Preview(showBackground = true)
@Composable
private fun ColorSettingsPreviewSectionPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        ColorSettingsPreviewSection(
            default01ColorLong = SettingsRepository.DEFAULT_01_COLOR_LONG,
            default04ColorLong = SettingsRepository.DEFAULT_04_COLOR_LONG,
            default07ColorLong = SettingsRepository.DEFAULT_07_COLOR_LONG,
            default10ColorLong = SettingsRepository.DEFAULT_10_COLOR_LONG,
            initiallyExpanded = true,
        )
    }
}
