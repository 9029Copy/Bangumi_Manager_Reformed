package com.copy9029.bangumimanagerreformed.ui.profile.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.util.calcCurrentSeason
import java.time.LocalDate

@Composable
internal fun SeasonPickerDialog(
    initialSeason: OverviewSeason,
    seasonOptions: List<OverviewSeasonOptionUiState>,
    onSeasonSelected: (OverviewSeason) -> Unit,
    onDismissRequest: () -> Unit,
    today: LocalDate = LocalDate.now(),
) {
    key(initialSeason, seasonOptions) {
        val nearestYearMonth = calcCurrentSeason(today)
        val nearestSeason = OverviewSeason(
            year = nearestYearMonth.year,
            month = nearestYearMonth.monthValue,
        )
        val initialIndex = seasonOptions
            .indexOfFirst { option -> option.season == initialSeason }
            .coerceAtLeast(0)
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = initialIndex,
        )

        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.profile_overview_select_season),
                        modifier = Modifier.padding(
                            start = 20.dp,
                            top = 18.dp,
                            end = 20.dp,
                            bottom = 12.dp,
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(
                            items = seasonOptions,
                            key = { option ->
                                "${option.season.year}-${option.season.month}"
                            },
                        ) { option ->
                            SeasonPickerItem(
                                uiState = option,
                                isSelected = option.season == initialSeason,
                                isNearestSeason = option.season == nearestSeason,
                                onClick = { onSeasonSelected(option.season) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonPickerItem(
    uiState: OverviewSeasonOptionUiState,
    isSelected: Boolean,
    isNearestSeason: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 28.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.profile_overview_picker_year,
                        uiState.season.year,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = fontWeight,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(
                        R.string.profile_overview_picker_month,
                        uiState.season.month,
                    ),
                    modifier = Modifier.width(38.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
                if (isNearestSeason) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Text(
                            text = stringResource(R.string.profile_overview_current_season),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    }
                }
            }
            Text(
                text = stringResource(
                    R.string.profile_overview_picker_item_count,
                    uiState.itemCount,
                ),
                modifier = Modifier.width(52.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(
    name = "Overview season picker",
    showBackground = true,
    widthDp = 400,
    heightDp = 720,
)
@Composable
private fun OverviewSeasonPickerDialogPreview() {
    val selectedSeason = OverviewSeason(year = 2026, month = 7)
    val itemCounts = mapOf(
        OverviewSeason(year = 2025, month = 1) to 3,
        OverviewSeason(year = 2025, month = 10) to 18,
        OverviewSeason(year = 2026, month = 4) to 7,
        selectedSeason to 12,
        OverviewSeason(year = 2027, month = 1) to 1,
    )
    val seasonOptions = (2025..2027).flatMap { year ->
        OVERVIEW_SEASON_MONTHS.map { month ->
            val season = OverviewSeason(year = year, month = month)
            OverviewSeasonOptionUiState(
                season = season,
                itemCount = itemCounts[season] ?: 0,
            )
        }
    }

    BangumiManagerReformedTheme {
        SeasonPickerDialog(
            initialSeason = selectedSeason,
            seasonOptions = seasonOptions,
            onSeasonSelected = {},
            onDismissRequest = {},
            today = LocalDate.of(2026, 9, 5),
        )
    }
}
