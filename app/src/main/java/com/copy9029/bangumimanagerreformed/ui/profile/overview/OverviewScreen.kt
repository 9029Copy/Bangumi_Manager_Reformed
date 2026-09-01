package com.copy9029.bangumimanagerreformed.ui.profile.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.components.wheel_picker.FVerticalWheelPicker
import com.copy9029.bangumimanagerreformed.ui.components.wheel_picker.rememberFWheelPickerState
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSeasonPickerVisible by rememberSaveable { mutableStateOf(false) }

    OverviewScreenContent(
        uiState = uiState,
        isSeasonPickerVisible = isSeasonPickerVisible,
        onBack = onBack,
        onPreviousSeasonClick = viewModel::onPreviousSeasonClick,
        onNextSeasonClick = viewModel::onNextSeasonClick,
        onSeasonClick = { isSeasonPickerVisible = true },
        onSeasonPickerDismiss = { isSeasonPickerVisible = false },
        onSeasonSelected = { season ->
            viewModel.onSeasonSelected(season)
            isSeasonPickerVisible = false
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverviewScreenContent(
    uiState: OverviewUiState,
    isSeasonPickerVisible: Boolean,
    onBack: () -> Unit,
    onPreviousSeasonClick: () -> Unit,
    onNextSeasonClick: () -> Unit,
    onSeasonClick: () -> Unit,
    onSeasonPickerDismiss: () -> Unit,
    onSeasonSelected: (OverviewSeason) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.selectedSeason) {
        listState.scrollToItem(0)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_overview_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SeasonSelectorRow(
                selectedSeason = uiState.selectedSeason,
                previousSeason = uiState.previousSeason,
                nextSeason = uiState.nextSeason,
                itemCount = uiState.items.size,
                isLoading = uiState.isLoading,
                onPreviousSeasonClick = onPreviousSeasonClick,
                onNextSeasonClick = onNextSeasonClick,
                onSeasonClick = onSeasonClick,
            )
            HorizontalDivider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    uiState.isLoading -> OverviewBodyMessage(
                        text = stringResource(R.string.profile_overview_loading),
                    )

                    uiState.items.isEmpty() -> OverviewBodyMessage(
                        text = stringResource(R.string.profile_overview_empty),
                    )

                    else -> LazyColumnScrollbar(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        settings = ScrollbarSettings.Default.copy(
                            thumbThickness = 4.dp,
                            scrollbarPadding = 2.dp,
                            thumbSelectedColor = MaterialTheme.colorScheme.onSurface,
                            thumbUnselectedColor = MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(top = 6.dp),
                        ) {
                            items(
                                items = uiState.items,
                                key = { item -> item.bangumiId },
                            ) { item ->
                                OverviewListItem(uiState = item)
                            }
                        }
                    }
                }
            }
        }
    }

    if (isSeasonPickerVisible) {
        SeasonPickerDialog(
            initialSeason = uiState.selectedSeason,
            minSeasonYear = uiState.minSeasonYear,
            maxSeasonYear = uiState.maxSeasonYear,
            onSeasonSelected = onSeasonSelected,
            onDismissRequest = onSeasonPickerDismiss,
        )
    }
}

@Composable
private fun SeasonSelectorRow(
    selectedSeason: OverviewSeason,
    previousSeason: OverviewSeason,
    nextSeason: OverviewSeason,
    itemCount: Int,
    isLoading: Boolean,
    onPreviousSeasonClick: () -> Unit,
    onNextSeasonClick: () -> Unit,
    onSeasonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val previousSeasonText = previousSeason.displayText()
    val nextSeasonText = nextSeason.displayText()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SeasonStepControl(
            seasonText = previousSeasonText,
            onClick = onPreviousSeasonClick,
            isPrevious = true,
            contentDescription = stringResource(
                R.string.profile_overview_previous_season,
                previousSeasonText,
            ),
            modifier = Modifier.weight(0.8f),
        )

        Column(
            modifier = Modifier
                .weight(1.6f)
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onSeasonClick)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = selectedSeason.displayText(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = when {
                    isLoading -> stringResource(R.string.profile_overview_loading)

                    else -> pluralStringResource(
                        R.plurals.profile_overview_item_count,
                        itemCount,
                        itemCount,
                    )
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        SeasonStepControl(
            seasonText = nextSeasonText,
            onClick = onNextSeasonClick,
            isPrevious = false,
            contentDescription = stringResource(
                R.string.profile_overview_next_season,
                nextSeasonText,
            ),
            modifier = Modifier.weight(0.8f),
        )
    }
}

@Composable
private fun OverviewBodyMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SeasonStepControl(
    seasonText: String,
    onClick: () -> Unit,
    isPrevious: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isPrevious) {
            Arrangement.Start
        } else {
            Arrangement.End
        },
    ) {
        if (isPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = seasonText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            textAlign = if (isPrevious) TextAlign.Start else TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (!isPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun SeasonPickerDialog(
    initialSeason: OverviewSeason,
    minSeasonYear: Int,
    maxSeasonYear: Int,
    onSeasonSelected: (OverviewSeason) -> Unit,
    onDismissRequest: () -> Unit,
) {
    key(initialSeason, minSeasonYear, maxSeasonYear) {
        val yearCount = maxSeasonYear - minSeasonYear + 1
        val initialYearIndex = initialSeason.year
            .coerceIn(minSeasonYear, maxSeasonYear) - minSeasonYear
        val initialMonthIndex = OVERVIEW_SEASON_MONTHS
            .indexOf(initialSeason.month)
            .coerceAtLeast(0)
        val yearPickerState = rememberFWheelPickerState(
            initialIndex = initialYearIndex,
        )
        val monthPickerState = rememberFWheelPickerState(
            initialIndex = initialMonthIndex,
        )

        Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
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
                    text = stringResource(R.string.profile_overview_select_season),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SeasonPickerHeight),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FVerticalWheelPicker(
                        modifier = Modifier.weight(1f),
                        count = yearCount,
                        state = yearPickerState,
                        key = { index -> minSeasonYear + index },
                        itemHeight = SeasonPickerItemHeight,
                        unfocusedCount = SeasonPickerUnfocusedCount,
                    ) { index ->
                        Text(
                            text = stringResource(
                                R.string.profile_overview_picker_year,
                                minSeasonYear + index,
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    FVerticalWheelPicker(
                        modifier = Modifier.weight(1f),
                        count = OVERVIEW_SEASON_MONTHS.size,
                        state = monthPickerState,
                        key = { index -> OVERVIEW_SEASON_MONTHS[index] },
                        itemHeight = SeasonPickerItemHeight,
                        unfocusedCount = SeasonPickerUnfocusedCount,
                    ) { index ->
                        Text(
                            text = stringResource(
                                R.string.profile_overview_picker_month,
                                OVERVIEW_SEASON_MONTHS[index],
                            ),
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
                            val yearIndex = yearPickerState.currentIndexSnapshot
                                .takeIf { it in 0 until yearCount }
                                ?: initialYearIndex
                            val monthIndex = monthPickerState.currentIndexSnapshot
                                .takeIf { it in OVERVIEW_SEASON_MONTHS.indices }
                                ?: initialMonthIndex
                            onSeasonSelected(
                                OverviewSeason(
                                    year = minSeasonYear + yearIndex,
                                    month = OVERVIEW_SEASON_MONTHS[monthIndex],
                                ),
                            )
                        },
                    ) {
                        Text(stringResource(R.string.action_confirm))
                    }
                }
            }
        }
    }
}
}

@Composable
private fun OverviewListItem(
    uiState: OverviewItemUiState,
    modifier: Modifier = Modifier,
) {
    val scoreText = uiState.scoreTimesTen?.let { scoreTimesTen ->
        (scoreTimesTen / 10.0).toString()
    } ?: stringResource(R.string.profile_overview_score_unknown)
    val hasScore = uiState.scoreTimesTen != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 26.dp)
            .padding(horizontal = 16.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = uiState.title,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier
                .width(56.dp)
                .padding(start = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = stringResource(R.string.profile_overview_score),
                modifier = Modifier.size(14.dp),
                tint = if (hasScore) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.outline
                },
            )
            Text(
                text = scoreText,
                modifier = Modifier.weight(1f),
                color = if (hasScore) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (hasScore) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun OverviewSeason.displayText(): String {
    return stringResource(
        R.string.profile_overview_season,
        year,
        month,
    )
}



@Preview(name = "加载中", showBackground = true)
@Composable
private fun OverviewLoadingPreview() {
    OverviewPreviewContent(
        uiState = OverviewUiState(
            selectedSeason = OverviewSeason(year = 2026, month = 7),
            isLoading = true,
        ),
    )
}

@Preview(name = "暂无项目", showBackground = true)
@Composable
private fun OverviewEmptyPreview() {
    OverviewPreviewContent(
        uiState = OverviewUiState(
            selectedSeason = OverviewSeason(year = 2026, month = 7),
            isLoading = false,
            items = emptyList(),
        ),
    )
}

@Preview(name = "正常", showBackground = true)
@Composable
private fun OverviewNormalPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        OverviewPreviewContent(uiState = overviewPreviewUiState())
    }
}

@Preview(name = "Dialog 显示", showBackground = true)
@Composable
private fun OverviewDialogPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        OverviewPreviewContent(
            uiState = overviewPreviewUiState(),
            isSeasonPickerVisible = true,
        )
    }
}

@Composable
private fun OverviewPreviewContent(
    uiState: OverviewUiState,
    isSeasonPickerVisible: Boolean = false,
) {
    BangumiManagerReformedTheme(dynamicColor = false) {
        OverviewScreenContent(
            uiState = uiState,
            isSeasonPickerVisible = isSeasonPickerVisible,
            onBack = {},
            onPreviousSeasonClick = {},
            onNextSeasonClick = {},
            onSeasonClick = {},
            onSeasonPickerDismiss = {},
            onSeasonSelected = {},
        )
    }
}

private fun overviewPreviewUiState(): OverviewUiState {
    return OverviewUiState(
        selectedSeason = OverviewSeason(year = 2026, month = 7),
        isLoading = false,
        items = listOf(
            OverviewItemUiState(
                bangumiId = 1,
                title = "季度项目示例一",
                scoreTimesTen = 92,
            ),
            OverviewItemUiState(
                bangumiId = 2,
                title = "一个标题稍长的季度项目示例",
                scoreTimesTen = 85,
            ),
            OverviewItemUiState(
                bangumiId = 3,
                title = "季度项目示例三",
                scoreTimesTen = 70,
            ),
            OverviewItemUiState(
                bangumiId = 4,
                title = "尚未评分的项目",
                scoreTimesTen = null,
            ),
        ),
    )
}

private const val SeasonPickerUnfocusedCount = 2
private val SeasonPickerItemHeight = 40.dp
private val SeasonPickerHeight =
    SeasonPickerItemHeight * (SeasonPickerUnfocusedCount * 2 + 1)
