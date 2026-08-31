package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BangumiEditScreen(
    viewModel: BangumiEditViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()

    if (uiState == null) {
        BangumiEditLoadingContent(
            onCancel = onBack,
            modifier = modifier,
        )
        return
    }

    BangumiEditContent(
        uiState = requireNotNull(uiState),
        onCancel = onBack,
        onSubmit = {
            coroutineScope.launch {
                val result = viewModel.onSubmitClick()
                if (result.messageRes == R.string.bangumi_edit_submit_success) {
                    Toast.makeText(
                        context,
                        resources.getString(result.messageRes),
                        Toast.LENGTH_SHORT,
                    ).show()
                    onBack()
                } else {
                    Toast.makeText(
                        context,
                        resources.getString(result.messageRes),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        },
        onTitleChanged = viewModel::onTitleChanged,
        onSeasonChanged = viewModel::onSeasonChanged,
        onFirstBroadcastDateChanged = viewModel::onFirstBroadcastDateChanged,
        onMyScoreChanged = viewModel::onMyScoreChanged,
        onHiddenChanged = viewModel::onHiddenChanged,
        onTotalEpisodesChanged = viewModel::onTotalEpisodesChanged,
        onLatestWatchedEpisodeChanged = viewModel::onLatestWatchedEpisodeChanged,
        onSetWatchedToMinimum = viewModel::onSetWatchedToMinimum,
        onWatchedEpisodeMinusOne = viewModel::onWatchedEpisodeMinusOne,
        onWatchedEpisodePlusOne = viewModel::onWatchedEpisodePlusOne,
        onSetWatchedToMaximum = viewModel::onSetWatchedToMaximum,
        onAddEpisodeBroadcastRule = viewModel::onAddEpisodeBroadcastRule,
        onDeleteEpisodeBroadcastRule = viewModel::onDeleteEpisodeBroadcastRule,
        onEpisodeBroadcastRuleEpisodeChanged =
            viewModel::onEpisodeBroadcastRuleEpisodeChanged,
        onEpisodeBroadcastRuleTypeChanged =
            viewModel::onEpisodeBroadcastRuleTypeChanged,
        onEpisodeBroadcastRuleDelayWeeksChanged =
            viewModel::onEpisodeBroadcastRuleDelayWeeksChanged,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BangumiEditLoadingContent(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bangumi_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {},
                        enabled = false,
                    ) {
                        Text(stringResource(R.string.action_submit))
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BangumiEditContent(
    uiState: BangumiEditUiState,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onSeasonChanged: (Int, Int) -> Unit,
    onFirstBroadcastDateChanged: (LocalDate) -> Unit,
    onMyScoreChanged: (String) -> Unit,
    onHiddenChanged: (Boolean) -> Unit,
    onTotalEpisodesChanged: (String) -> Unit,
    onLatestWatchedEpisodeChanged: (String) -> Unit,
    onSetWatchedToMinimum: () -> Unit,
    onWatchedEpisodeMinusOne: () -> Unit,
    onWatchedEpisodePlusOne: () -> Unit,
    onSetWatchedToMaximum: () -> Unit,
    onAddEpisodeBroadcastRule: () -> Unit,
    onDeleteEpisodeBroadcastRule: (Long) -> Unit,
    onEpisodeBroadcastRuleEpisodeChanged: (Long, String) -> Unit,
    onEpisodeBroadcastRuleTypeChanged: (Long, EpisodeBroadcastRuleType) -> Unit,
    onEpisodeBroadcastRuleDelayWeeksChanged: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bangumi_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSubmit,
                        enabled = !uiState.isSubmitting,
                    ) {
                        Text(stringResource(R.string.action_submit))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                BangumiBasicInfoEditSection(
                    uiState = uiState,
                    onTitleChanged = onTitleChanged,
                    onSeasonChanged = onSeasonChanged,
                    onFirstBroadcastDateChanged = onFirstBroadcastDateChanged,
                    onMyScoreChanged = onMyScoreChanged,
                    onHiddenChanged = onHiddenChanged,
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                BangumiProgressEditSection(
                    uiState = uiState,
                    onTotalEpisodesChanged = onTotalEpisodesChanged,
                    onLatestWatchedEpisodeChanged = onLatestWatchedEpisodeChanged,
                    onSetWatchedToMinimum = onSetWatchedToMinimum,
                    onWatchedEpisodeMinusOne = onWatchedEpisodeMinusOne,
                    onWatchedEpisodePlusOne = onWatchedEpisodePlusOne,
                    onSetWatchedToMaximum = onSetWatchedToMaximum,
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                BangumiScheduleEditSection(
                    rules = uiState.episodeBroadcastRules,
                    onAddRule = onAddEpisodeBroadcastRule,
                    onDeleteRule = onDeleteEpisodeBroadcastRule,
                    onEpisodeChanged = onEpisodeBroadcastRuleEpisodeChanged,
                    onRuleTypeChanged = onEpisodeBroadcastRuleTypeChanged,
                    onDelayWeeksChanged = onEpisodeBroadcastRuleDelayWeeksChanged,
                )
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }

        }
    }
}

@Preview(
    showBackground = true,
    heightDp = 1200,
)
@Composable
private fun PreviewBangumiEditContent() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiEditContent(
            uiState = BangumiEditUiState(
                bangumiId = 1,
                title = "示例动画标题",
                seasonYear = 2026,
                seasonMonth = 7,
                seasonStartYear = 2021,
                seasonEndYear = 2028,
                firstBroadcastDate = LocalDate.of(2026, 7, 5),
                myScoreInput = "8.5",
                isActive = true,
                totalEpisodesInput = "12",
                latestWatchedEpisodeInput = "5",
                latestAiredEpisode = 7,
                episodeBroadcastRules = listOf(
                    EpisodeBroadcastRuleUiState(
                        rowId = 1,
                        episodeInput = "5",
                        ruleType = EpisodeBroadcastRuleType.DELAY,
                        delayWeeksInput = "1",
                    ),
                    EpisodeBroadcastRuleUiState(
                        rowId = 2,
                        episodeInput = "8",
                        ruleType = EpisodeBroadcastRuleType.SAME_DAY_AS_PREVIOUS,
                    ),
                ),
            ),
            onCancel = {},
            onSubmit = {},
            onTitleChanged = {},
            onSeasonChanged = { _, _ -> },
            onFirstBroadcastDateChanged = {},
            onMyScoreChanged = {},
            onHiddenChanged = {},
            onTotalEpisodesChanged = {},
            onLatestWatchedEpisodeChanged = {},
            onSetWatchedToMinimum = {},
            onWatchedEpisodeMinusOne = {},
            onWatchedEpisodePlusOne = {},
            onSetWatchedToMaximum = {},
            onAddEpisodeBroadcastRule = {},
            onDeleteEpisodeBroadcastRule = {},
            onEpisodeBroadcastRuleEpisodeChanged = { _, _ -> },
            onEpisodeBroadcastRuleTypeChanged = { _, _ -> },
            onEpisodeBroadcastRuleDelayWeeksChanged = { _, _ -> },
        )
    }
}

@Preview(
    showBackground = true,
    heightDp = 1200,
)
@Composable
private fun PreviewBangumiEditContent2() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiEditContent(
            uiState = BangumiEditUiState(
                bangumiId = 1,
                title = "示例动画标题",
                seasonYear = 2026,
                seasonMonth = 7,
                seasonStartYear = 2021,
                seasonEndYear = 2028,
                firstBroadcastDate = LocalDate.of(2026, 7, 5),
                myScoreInput = "8.5",
                isActive = true,
                totalEpisodesInput = "12",
                latestWatchedEpisodeInput = "5",
                latestAiredEpisode = 7,
                titleError = R.string.bangumi_error_title_required,
                myScoreError = R.string.bangumi_edit_error_score_format,
                totalEpisodesError = R.string.bangumi_edit_error_total_episodes_positive,
                latestWatchedEpisodeError =
                    R.string.bangumi_edit_error_watched_episode_nonnegative_integer,
            ),
            onCancel = {},
            onSubmit = {},
            onTitleChanged = {},
            onSeasonChanged = { _, _ -> },
            onFirstBroadcastDateChanged = {},
            onMyScoreChanged = {},
            onHiddenChanged = {},
            onTotalEpisodesChanged = {},
            onLatestWatchedEpisodeChanged = {},
            onSetWatchedToMinimum = {},
            onWatchedEpisodeMinusOne = {},
            onWatchedEpisodePlusOne = {},
            onSetWatchedToMaximum = {},
            onAddEpisodeBroadcastRule = {},
            onDeleteEpisodeBroadcastRule = {},
            onEpisodeBroadcastRuleEpisodeChanged = { _, _ -> },
            onEpisodeBroadcastRuleTypeChanged = { _, _ -> },
            onEpisodeBroadcastRuleDelayWeeksChanged = { _, _ -> },
        )
    }
}
