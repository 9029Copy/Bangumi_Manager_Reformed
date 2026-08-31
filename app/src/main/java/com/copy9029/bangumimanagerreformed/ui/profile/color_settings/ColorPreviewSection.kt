package com.copy9029.bangumimanagerreformed.ui.profile.color_settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
internal fun ColorPreviewSection(
    default01ColorLong: Long,
    default04ColorLong: Long,
    default07ColorLong: Long,
    default10ColorLong: Long,
    modifier: Modifier = Modifier,
) {
    val monthNames = stringArrayResource(R.array.calendar_month_names)
    val previewItems = listOf(
        ColorPreviewItem(
            title = stringResource(
                R.string.profile_color_preview_item_title,
                monthNames[0],
            ),
            colorLong = default01ColorLong,
        ),
        ColorPreviewItem(
            title = stringResource(
                R.string.profile_color_preview_item_title,
                monthNames[3],
            ),
            colorLong = default04ColorLong,
        ),
        ColorPreviewItem(
            title = stringResource(
                R.string.profile_color_preview_item_title,
                monthNames[6],
            ),
            colorLong = default07ColorLong,
        ),
        ColorPreviewItem(
            title = stringResource(
                R.string.profile_color_preview_item_title,
                monthNames[9],
            ),
            colorLong = default10ColorLong,
        ),
    )
    val indexSubtitle = BangumiWatchProgressUiState(
        dayOfWeek = 1,
        latestWatchedEpisode = 1,
        latestAiredEpisode = 12,
        totalEpisodes = 12,
        startDate = LocalDate.of(2025, 1, 1),
    ).displayText()

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.profile_color_preview_title),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(10.dp))
        previewItems.forEach { item ->
            ColorPreviewIndexItem(
                item = item,
                subtitle = indexSubtitle,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        ColorPreviewCalendarWeekRow(
            items = previewItems,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
    }
}

private data class ColorPreviewItem(
    val title: String,
    val colorLong: Long,
)

@Composable
private fun ColorPreviewIndexItem(
    item: ColorPreviewItem,
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
                ColorPreviewIndexAction(
                    painter = painterResource(R.drawable.index_item_button_plus_1),
                    colorLong = item.colorLong,
                )
                ColorPreviewIndexAction(
                    painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                        Icons.Filled.Edit,
                    ),
                    colorLong = item.colorLong,
                )
                ColorPreviewIndexAction(
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
private fun ColorPreviewIndexAction(
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
private fun ColorPreviewCalendarWeekRow(
    items: List<ColorPreviewItem>,
    modifier: Modifier = Modifier,
) {
    val januaryName = stringArrayResource(R.array.calendar_month_names)[0]

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 2.dp, vertical = 2.dp),
    ) {
        repeat(7) { index ->
            val dateLabel: ColorPreviewDateLabel = when (index) {
                0 -> ColorPreviewDateLabel.Today
                1 -> ColorPreviewDateLabel.Month(januaryName)
                else -> ColorPreviewDateLabel.Day(index)
            }

            ColorPreviewCalendarDateCell(
                dateLabel = dateLabel,
                item = items.getOrNull(index),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

private sealed interface ColorPreviewDateLabel {
    data object Today : ColorPreviewDateLabel
    data class Month(val text: String) : ColorPreviewDateLabel
    data class Day(val dayOfMonth: Int) : ColorPreviewDateLabel
}

@Composable
private fun ColorPreviewCalendarDateCell(
    dateLabel: ColorPreviewDateLabel,
    item: ColorPreviewItem?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(1.dp),
        shape = RoundedCornerShape(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
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
                    ColorPreviewDateLabel.Today -> ColorPreviewTodayLabel()
                    is ColorPreviewDateLabel.Month -> ColorPreviewMonthLabel(dateLabel.text)
                    is ColorPreviewDateLabel.Day -> Text(
                        text = dateLabel.dayOfMonth.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (item != null) {
                ColorPreviewCalendarTag(item)
            }
        }
    }
}

@Composable
private fun ColorPreviewTodayLabel() {
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
private fun ColorPreviewMonthLabel(text: String) {
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
private fun ColorPreviewCalendarTag(
    item: ColorPreviewItem,
    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(item.colorLong)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.calendarUnfinishedTagContainer,
        shape = RoundedCornerShape(3.dp),
    ) {
        Text(
            text = item.title,
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
private fun ColorPreviewSectionPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        ColorPreviewSection(
            default01ColorLong = SettingsRepository.DEFAULT_01_COLOR_LONG,
            default04ColorLong = SettingsRepository.DEFAULT_04_COLOR_LONG,
            default07ColorLong = SettingsRepository.DEFAULT_07_COLOR_LONG,
            default10ColorLong = SettingsRepository.DEFAULT_10_COLOR_LONG,
        )
    }
}
