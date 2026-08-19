package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.components.AppDatePickerDialog
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.SeasonSelectSection
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    EditSectionTitle(stringResource(R.string.bangumi_edit_section_basic_info))

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = onTitleChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.bangumi_field_title)) },
                        singleLine = true,
                        isError = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { error ->
                            { Text(stringResource(error)) }
                        },
                    )

                    SeasonSelectSection(
                        startYear = uiState.seasonStartYear,
                        endYear = uiState.seasonEndYear,
                        selectedYear = uiState.seasonYear,
                        selectedMonth = uiState.seasonMonth,
                        onSeasonSelected = onSeasonChanged,
                        colorLong = uiState.themeColorLong,
                    )

                    FirstBroadcastDateEditRow(
                        date = uiState.firstBroadcastDate,
                        onDateSelected = onFirstBroadcastDateChanged,
                        enabled = uiState.episodeBroadcastRules != null,
                    )

                    EditFormRow(label = stringResource(R.string.bangumi_edit_my_score)) {
                        OutlinedTextField(
                            value = uiState.myScoreInput,
                            onValueChange = onMyScoreChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.bangumi_edit_score_unspecified)) },
                            suffix = { Text(stringResource(R.string.bangumi_edit_score_suffix)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                            ),
                            isError = uiState.myScoreError != null,
                            supportingText = uiState.myScoreError?.let { error ->
                                { Text(stringResource(error)) }
                            },
                        )
                    }

                    EditFormRow(label = stringResource(R.string.bangumi_edit_visibility)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(
                                    if (uiState.isActive) {
                                        R.string.bangumi_edit_visibility_visible
                                    } else {
                                        R.string.bangumi_edit_visibility_hidden
                                    },
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = !uiState.isActive,
                                onCheckedChange = onHiddenChanged,
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    EditSectionTitle(stringResource(R.string.bangumi_edit_section_progress))

                    EditFormRow(label = stringResource(R.string.bangumi_edit_total_episodes)) {
                        OutlinedTextField(
                            value = uiState.totalEpisodesInput,
                            onValueChange = onTotalEpisodesChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.bangumi_edit_total_episodes_unspecified)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                            ),
                            isError = uiState.totalEpisodesError != null,
                            supportingText = if (
                                uiState.totalEpisodesError != null ||
                                uiState.totalEpisodesInput.isBlank()
                            ) {
                                {
                                    Text(
                                        text = uiState.totalEpisodesError?.let {
                                            stringResource(it)
                                        } ?: stringResource(
                                            R.string.bangumi_edit_total_episodes_currently_unspecified
                                        ),
                                        color = if (uiState.totalEpisodesError != null) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.bangumi_edit_watched_episodes),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = stringResource(
                                    R.string.bangumi_edit_latest_aired_episode,
                                    uiState.latestAiredEpisode,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = onSetWatchedToMinimum,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) {
                                Text(stringResource(R.string.bangumi_edit_set_minimum))
                            }
                            OutlinedButton(
                                onClick = onWatchedEpisodeMinusOne,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) {
                                Text("−")
                            }
                            OutlinedTextField(
                                value = uiState.latestWatchedEpisodeInput,
                                onValueChange = onLatestWatchedEpisodeChanged,
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    textAlign = TextAlign.Center,
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                ),
                                isError = uiState.latestWatchedEpisodeError != null,
                            )
                            OutlinedButton(
                                onClick = onWatchedEpisodePlusOne,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) {
                                Text("+")
                            }
                            OutlinedButton(
                                onClick = onSetWatchedToMaximum,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) {
                                Text(stringResource(R.string.bangumi_edit_set_maximum))
                            }
                        }

                        uiState.latestWatchedEpisodeError?.let { error ->
                            Text(
                                text = stringResource(error),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                EditSectionTitle(
                    stringResource(R.string.bangumi_edit_section_broadcast_adjustment)
                )
                
                BangumiScheduleRuleEditor(
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

@Composable
private fun EditSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun EditFormRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(88.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirstBroadcastDateEditRow(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    EditFormRow(
        label = stringResource(R.string.bangumi_field_first_broadcast_date),
        modifier = modifier,
    ) {
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Text(date.toString())
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDate = date,
            onDateSelected = onDateSelected,
            onDismissRequest = {
                showDatePicker = false
            },
        )
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
