package com.copy9029.bangumimanagerreformed.ui.profile.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OverviewScreenContent(
        uiState = uiState,
        onBack = onBack,
        onPreviousSeasonClick = viewModel::onPreviousSeasonClick,
        onNextSeasonClick = viewModel::onNextSeasonClick,
        onSeasonClick = viewModel::onSeasonClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverviewScreenContent(
    uiState: OverviewUiState,
    onBack: () -> Unit,
    onPreviousSeasonClick: () -> Unit,
    onNextSeasonClick: () -> Unit,
    onSeasonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                onPreviousSeasonClick = onPreviousSeasonClick,
                onNextSeasonClick = onNextSeasonClick,
                onSeasonClick = onSeasonClick,
            )
            HorizontalDivider()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }
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

@Composable
private fun SeasonSelectorRow(
    selectedSeason: OverviewSeason,
    previousSeason: OverviewSeason,
    nextSeason: OverviewSeason,
    itemCount: Int,
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
            modifier = Modifier.weight(1f),
        )

        Column(
            modifier = Modifier
                .weight(1.2f)
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
                text = pluralStringResource(
                    R.plurals.profile_overview_item_count,
                    itemCount,
                    itemCount,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
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
            modifier = Modifier.weight(1f),
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
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (isPrevious) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = contentDescription,
                )
            }
        }
        Text(
            text = seasonText,
            modifier = Modifier.weight(1f, fill = false),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (!isPrevious) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = contentDescription,
                )
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

@Preview(showBackground = true)
@Composable
private fun OverviewScreenPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        OverviewScreenContent(
            uiState = OverviewUiState(
                selectedSeason = OverviewSeason(year = 2026, month = 7),
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
            ),
            onBack = {},
            onPreviousSeasonClick = {},
            onNextSeasonClick = {},
            onSeasonClick = {},
        )
    }
}
