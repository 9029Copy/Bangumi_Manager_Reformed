package com.copy9029.bangumimanagerreformed.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                title = { Text("日历设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { isResetDialogVisible = true }) {
                        Text("恢复默认")
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
            item { SettingsSectionTitle("显示") }
            item {
                Text(
                    text = "隐藏项目",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
            item {
                InactiveVisibilityOption(
                    title = "不显示",
                    selected = uiState.calendarInactiveVisibility ==
                        CalendarInactiveVisibilityDefaults.ACTIVE,
                    onClick = {
                        onInactiveVisibilityChanged(CalendarInactiveVisibilityDefaults.ACTIVE)
                    },
                )
            }
            item {
                InactiveVisibilityOption(
                    title = "显示",
                    selected = uiState.calendarInactiveVisibility ==
                        CalendarInactiveVisibilityDefaults.ALL,
                    onClick = {
                        onInactiveVisibilityChanged(CalendarInactiveVisibilityDefaults.ALL)
                    },
                )
            }
            item {
                InactiveVisibilityOption(
                    title = "仅显示",
                    selected = uiState.calendarInactiveVisibility ==
                        CalendarInactiveVisibilityDefaults.INACTIVE,
                    onClick = {
                        onInactiveVisibilityChanged(CalendarInactiveVisibilityDefaults.INACTIVE)
                    },
                )
            }
            item {
                SwitchSettingItem(
                    title = "显示已看过的单集",
                    checked = uiState.calendarFinishedEpisodeVisible,
                    onCheckedChange = onFinishedEpisodeVisibleChanged,
                )
            }
            item {
                SwitchSettingItem(
                    title = "显示已完成番剧",
                    checked = uiState.calendarFinishedBangumiVisible,
                    onCheckedChange = onFinishedBangumiVisibleChanged,
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                SettingsSectionTitle("日历范围")
            }
            item {
                NumberSettingItem(
                    title = "当前周之前",
                    supportingText = "日历向过去加载的周数",
                    value = uiState.calendarWeeksBeforeCurrent,
                    valueRange = 52..2600,
                    onValueChange = onWeeksBeforeCurrentChanged,
                )
            }
            item {
                NumberSettingItem(
                    title = "当前周之后",
                    supportingText = "日历向未来加载的周数",
                    value = uiState.calendarWeeksAfterCurrent,
                    valueRange = 52..2600,
                    onValueChange = onWeeksAfterCurrentChanged,
                )
            }
            item {
                NumberSettingItem(
                    title = "初始位置提前周数",
                    supportingText = "打开日历时在当前周之前保留的周数",
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
            title = { Text("恢复默认设置？") },
            text = { Text("日历的所有设置都将恢复为默认值。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isResetDialogVisible = false
                        onResetToDefaults()
                    },
                ) {
                    Text("恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { isResetDialogVisible = false }) {
                    Text("取消")
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
                    label = { Text("周数") },
                    supportingText = {
                        Text("请输入 ${valueRange.first} 到 ${valueRange.last}")
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
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { isInputDialogVisible = false }) {
                    Text("取消")
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
