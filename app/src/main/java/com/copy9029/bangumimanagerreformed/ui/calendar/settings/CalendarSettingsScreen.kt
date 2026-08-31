package com.copy9029.bangumimanagerreformed.ui.calendar.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.CalendarInactiveVisibilityDefaults
import com.copy9029.bangumimanagerreformed.data.CalendarSettings
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme

@Composable
fun CalendarSettingsScreen(
    viewModel: CalendarSettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarSettingsScreenContent(
        uiState = uiState,
        onBack = onBack,
        onInactiveVisibilityChanged = viewModel::setInactiveVisibility,
        onFinishedEpisodeVisibleChanged = viewModel::setFinishedEpisodeVisible,
        onFinishedBangumiVisibleChanged = viewModel::setFinishedBangumiVisible,
        onWeeksBeforeCurrentChanged = viewModel::setWeeksBeforeCurrent,
        onWeeksAfterCurrentChanged = viewModel::setWeeksAfterCurrent,
        onWeeksPrefixChanged = viewModel::setWeeksPrefix,
        onResetToDefaults = viewModel::resetToDefaults,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarSettingsScreenContent(
    uiState: CalendarSettings,
    onBack: () -> Unit,
    onInactiveVisibilityChanged: (Int) -> Unit,
    onFinishedEpisodeVisibleChanged: (Boolean) -> Unit,
    onFinishedBangumiVisibleChanged: (Boolean) -> Unit,
    onWeeksBeforeCurrentChanged: (Int) -> Unit,
    onWeeksAfterCurrentChanged: (Int) -> Unit,
    onWeeksPrefixChanged: (Int) -> Unit,
    onResetToDefaults: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isResetDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_calendar_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { isResetDialogVisible = true }) {
                        Text(stringResource(R.string.settings_restore_defaults))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { SettingsSectionTitle(stringResource(R.string.settings_section_display)) }
            item {
                Text(
                    text = stringResource(R.string.settings_hidden_items),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
            item {
                InactiveVisibilityOption(
                    title = stringResource(R.string.settings_hidden_items_exclude),
                    selected = uiState.calendarInactiveVisibility ==
                        CalendarInactiveVisibilityDefaults.ACTIVE,
                    onClick = {
                        onInactiveVisibilityChanged(CalendarInactiveVisibilityDefaults.ACTIVE)
                    },
                )
            }
            item {
                InactiveVisibilityOption(
                    title = stringResource(R.string.settings_hidden_items_include),
                    selected = uiState.calendarInactiveVisibility ==
                        CalendarInactiveVisibilityDefaults.ALL,
                    onClick = {
                        onInactiveVisibilityChanged(CalendarInactiveVisibilityDefaults.ALL)
                    },
                )
            }
            item {
                InactiveVisibilityOption(
                    title = stringResource(R.string.settings_hidden_items_only),
                    selected = uiState.calendarInactiveVisibility ==
                        CalendarInactiveVisibilityDefaults.INACTIVE,
                    onClick = {
                        onInactiveVisibilityChanged(CalendarInactiveVisibilityDefaults.INACTIVE)
                    },
                )
            }
            item {
                SwitchSettingItem(
                    title = stringResource(R.string.settings_show_watched_episodes),
                    checked = uiState.calendarFinishedEpisodeVisible,
                    onCheckedChange = onFinishedEpisodeVisibleChanged,
                )
            }
            item {
                SwitchSettingItem(
                    title = stringResource(R.string.settings_show_completed_bangumis),
                    checked = uiState.calendarFinishedBangumiVisible,
                    onCheckedChange = onFinishedBangumiVisibleChanged,
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                SettingsSectionTitle(stringResource(R.string.settings_section_calendar_range))
            }
            item {
                NumberSettingItem(
                    title = stringResource(R.string.settings_weeks_before_current),
                    supportingText = stringResource(
                        R.string.settings_weeks_before_current_description
                    ),
                    value = uiState.calendarWeeksBeforeCurrent,
                    valueRange = IntRange(
                        start = CalendarSettingsViewModel.MIN_WEEK_COUNT,
                        endInclusive = CalendarSettingsViewModel.MAX_WEEK_COUNT
                    ),
                    onValueChange = onWeeksBeforeCurrentChanged,
                )
            }
            item {
                NumberSettingItem(
                    title = stringResource(R.string.settings_weeks_after_current),
                    supportingText = stringResource(
                        R.string.settings_weeks_after_current_description
                    ),
                    value = uiState.calendarWeeksAfterCurrent,
                    valueRange = IntRange(
                        start = CalendarSettingsViewModel.MIN_WEEK_COUNT,
                        endInclusive = CalendarSettingsViewModel.MAX_WEEK_COUNT
                    ),
                    onValueChange = onWeeksAfterCurrentChanged,
                )
            }
            item {
                NumberSettingItem(
                    title = stringResource(R.string.settings_initial_weeks_prefix),
                    supportingText = stringResource(
                        R.string.settings_initial_weeks_prefix_description
                    ),
                    value = uiState.calendarWeeksPrefix,
                    valueRange = 0..10,
                    onValueChange = onWeeksPrefixChanged,
                )
            }
        }
    }

    if (isResetDialogVisible) {
        AlertDialog(
            onDismissRequest = { isResetDialogVisible = false },
            title = { Text(stringResource(R.string.settings_restore_defaults_title)) },
            text = { Text(stringResource(R.string.settings_restore_defaults_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        isResetDialogVisible = false
                        onResetToDefaults()
                    },
                ) {
                    Text(stringResource(R.string.settings_restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { isResetDialogVisible = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun InactiveVisibilityOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) },
    )
}

@Composable
private fun NumberSettingItem(
    title: String,
    supportingText: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
) {
    var isInputDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var inputText by rememberSaveable {
        mutableStateOf(value.toString())
    }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(supportingText) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onValueChange(value - 1) },
                    enabled = value > valueRange.first,
                ) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Text(
                    text = value.toString(),
                    modifier = Modifier
                        .clickable {
                            inputText = value.toString()
                            isInputDialogVisible = true
                        }
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(
                    onClick = { onValueChange(value + 1) },
                    enabled = value < valueRange.last,
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
        },
    )

    if (isInputDialogVisible) {
        val inputValue = inputText.toIntOrNull()
        val isInputValid = inputValue != null && inputValue in valueRange

        AlertDialog(
            onDismissRequest = { isInputDialogVisible = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { text ->
                        if (text.all { it.isDigit() }) {
                            inputText = text
                        }
                    },
                    label = { Text(stringResource(R.string.settings_week_count)) },
                    supportingText = {
                        Text(
                            stringResource(
                                R.string.settings_number_range_hint,
                                valueRange.first,
                                valueRange.last,
                            )
                        )
                    },
                    isError = inputText.isNotEmpty() && !isInputValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(requireNotNull(inputValue))
                        isInputDialogVisible = false
                    },
                    enabled = isInputValid,
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isInputDialogVisible = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun CalendarSettingsScreenPreview() {
    BangumiManagerReformedTheme {
        CalendarSettingsScreenContent(
            uiState = CalendarSettings(
                calendarInactiveVisibility = CalendarInactiveVisibilityDefaults.ACTIVE,
                calendarFinishedEpisodeVisible = true,
                calendarFinishedBangumiVisible = false,
                calendarWeeksBeforeCurrent = 156,
                calendarWeeksAfterCurrent = 156,
                calendarWeeksPrefix = 1,
            ),
            onBack = {},
            onInactiveVisibilityChanged = {},
            onFinishedEpisodeVisibleChanged = {},
            onFinishedBangumiVisibleChanged = {},
            onWeeksBeforeCurrentChanged = {},
            onWeeksAfterCurrentChanged = {},
            onWeeksPrefixChanged = {},
            onResetToDefaults = {},
        )
    }
}
