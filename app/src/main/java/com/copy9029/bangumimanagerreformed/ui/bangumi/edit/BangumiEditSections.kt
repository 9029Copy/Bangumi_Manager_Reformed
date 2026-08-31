package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.SeasonSelectSection
import com.copy9029.bangumimanagerreformed.ui.components.AppDatePickerDialog
import java.time.LocalDate

@Composable
internal fun BangumiBasicInfoEditSection(
    uiState: BangumiEditUiState,
    onTitleChanged: (String) -> Unit,
    onSeasonChanged: (Int, Int) -> Unit,
    onFirstBroadcastDateChanged: (LocalDate) -> Unit,
    onMyScoreChanged: (String) -> Unit,
    onHiddenChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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

@Composable
internal fun BangumiProgressEditSection(
    uiState: BangumiEditUiState,
    onTotalEpisodesChanged: (String) -> Unit,
    onLatestWatchedEpisodeChanged: (String) -> Unit,
    onSetWatchedToMinimum: () -> Unit,
    onWatchedEpisodeMinusOne: () -> Unit,
    onWatchedEpisodePlusOne: () -> Unit,
    onSetWatchedToMaximum: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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

@Composable
internal fun BangumiScheduleEditSection(
    rules: List<EpisodeBroadcastRuleUiState>?,
    onAddRule: () -> Unit,
    onDeleteRule: (Long) -> Unit,
    onEpisodeChanged: (Long, String) -> Unit,
    onRuleTypeChanged: (Long, EpisodeBroadcastRuleType) -> Unit,
    onDelayWeeksChanged: (Long, String) -> Unit,
) {
    EditSectionTitle(
        stringResource(R.string.bangumi_edit_section_broadcast_adjustment)
    )

    BangumiScheduleRuleEditor(
        rules = rules,
        onAddRule = onAddRule,
        onDeleteRule = onDeleteRule,
        onEpisodeChanged = onEpisodeChanged,
        onRuleTypeChanged = onRuleTypeChanged,
        onDelayWeeksChanged = onDelayWeeksChanged,
    )
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



