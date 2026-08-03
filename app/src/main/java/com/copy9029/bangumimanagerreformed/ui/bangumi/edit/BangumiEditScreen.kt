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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.SeasonSelectSection
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun BangumiEditScreen(
    viewModel: BangumiEditViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
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
                if (result == BangumiEditViewModel.SUBMIT_SUCCESS) {
                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
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
                title = { Text("编辑项目") },
                actions = {
                    TextButton(onClick = onCancel) {
                        Text("取消")
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
                title = { Text("编辑项目") },
                actions = {
                    TextButton(onClick = onCancel) {
                        Text("取消")
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
                    EditSectionTitle("基本信息")

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = onTitleChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("标题") },
                        singleLine = true,
                        isError = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { error ->
                            { Text(error) }
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

                    EditFormRow(label = "我的评分") {
                        OutlinedTextField(
                            value = uiState.myScoreInput,
                            onValueChange = onMyScoreChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("未评分") },
                            suffix = { Text("/ 10.0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                            ),
                            isError = uiState.myScoreError != null,
                            supportingText = uiState.myScoreError?.let { error ->
                                { Text(error) }
                            },
                        )
                    }

                    EditFormRow(label = "是否隐藏") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (uiState.isActive) "正常显示" else "隐藏",
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
                    EditSectionTitle("集数与观看进度")

                    EditFormRow(label = "总集数") {
                        OutlinedTextField(
                            value = uiState.totalEpisodesInput,
                            onValueChange = onTotalEpisodesChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("未定") },
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
                                        text = uiState.totalEpisodesError ?: "当前为未定",
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
                                text = "已观看集数",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "当前更新到第 ${uiState.latestAiredEpisode} 集",
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
                                Text("最小")
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
                                Text("最大")
                            }
                        }

                        uiState.latestWatchedEpisodeError?.let { error ->
                            Text(
                                text = error,
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
                EditSectionTitle("播出日期调整")
                
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
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("提交")
                }
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
        label = "开播日期",
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                            )
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = datePickerState)
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
                titleError = "titleError text",
                myScoreError = "myScoreError text",
                totalEpisodesError = "totalEpisodesError text",
                latestWatchedEpisodeError = "latestWatchedEpisodeError text",
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
