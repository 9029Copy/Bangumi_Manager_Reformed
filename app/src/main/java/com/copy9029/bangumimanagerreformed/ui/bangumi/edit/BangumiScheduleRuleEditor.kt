package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme

@Composable
fun BangumiScheduleRuleEditor(
    rules: List<EpisodeBroadcastRuleUiState>?,
    onAddRule: () -> Unit,
    onDeleteRule: (Long) -> Unit,
    onEpisodeChanged: (Long, String) -> Unit,
    onRuleTypeChanged: (Long, EpisodeBroadcastRuleType) -> Unit,
    onDelayWeeksChanged: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        ScheduleRuleHeader()

        if (rules.isNullOrEmpty()) {
            Text(
                text = if ((rules == null)) "日期锚点解析失败，无法编辑播出规则" else "暂无播出日期调整",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EMPTY_STATE_HEIGHT_DP.dp)
                    .padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        } else {
            rules.forEach { rule ->
                ScheduleRuleRow(
                    rule = rule,
                    onEpisodeChanged = {
                        onEpisodeChanged(rule.rowId, it)
                    },
                    onRuleTypeChanged = {
                        onRuleTypeChanged(rule.rowId, it)
                    },
                    onDelayWeeksChanged = {
                        onDelayWeeksChanged(rule.rowId, it)
                    },
                    onDelete = {
                        onDeleteRule(rule.rowId)
                    },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ADD_BUTTON_HEIGHT_DP.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onAddRule,
                enabled = rules != null
            ) {
                Text("+ 添加行")
            }
        }
    }
}

@Composable
private fun ScheduleRuleHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HEADER_HEIGHT_DP.dp)
            .padding(horizontal = ROW_HORIZONTAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "集数",
            modifier = Modifier.width(EPISODE_COLUMN_WIDTH),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = "播出规则",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(ACTION_COLUMN_WIDTH))
    }
}

@Composable
private fun ScheduleRuleRow(
    rule: EpisodeBroadcastRuleUiState,
    onEpisodeChanged: (String) -> Unit,
    onRuleTypeChanged: (EpisodeBroadcastRuleType) -> Unit,
    onDelayWeeksChanged: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(RULE_ROW_HEIGHT_DP.dp)
            .padding(horizontal = ROW_HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = rule.episodeInput,
            onValueChange = onEpisodeChanged,
            modifier = Modifier
                .width(EPISODE_COLUMN_WIDTH)
                .height(INPUT_HEIGHT),
            placeholder = { Text(text = "请输入", fontSize = 12.sp)},
            singleLine = true,
            isError = rule.episodeError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
            ),
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScheduleRuleTypeDropdown(
                selectedType = rule.ruleType,
                onRuleTypeSelected = onRuleTypeChanged,
                isError = rule.ruleError != null,
                modifier = Modifier.weight(1.2f),
            )

            if (rule.ruleType == EpisodeBroadcastRuleType.DELAY) {
                OutlinedTextField(
                    value = rule.delayWeeksInput,
                    onValueChange = onDelayWeeksChanged,
                    modifier = Modifier
                        .height(INPUT_HEIGHT)
                        .weight(1f),
                    placeholder = { Text(text = "请输入", fontSize = 12.sp) },
                    suffix = { Text("周") },
                    singleLine = true,
                    isError = rule.delayWeeksError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.width(ACTION_COLUMN_WIDTH),
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "删除该行",
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = DIVIDER_VERTICAL_PADDING),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleRuleTypeDropdown(
    selectedType: EpisodeBroadcastRuleType?,
    onRuleTypeSelected: (EpisodeBroadcastRuleType) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedType?.displayText().orEmpty(),
            onValueChange = {},
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .height(INPUT_HEIGHT),
            placeholder = { Text(text = "请选择规则", fontSize = 12.sp) },
            readOnly = true,
            singleLine = true,
            isError = isError,
            textStyle = MaterialTheme.typography.bodyMedium,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            EpisodeBroadcastRuleType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(type.displayText())
                    },
                    onClick = {
                        onRuleTypeSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun EpisodeBroadcastRuleType.displayText(): String {
    return when (this) {
        EpisodeBroadcastRuleType.DELAY -> "停更"
        EpisodeBroadcastRuleType.SAME_DAY_AS_PREVIOUS -> "与上一集同日播出"
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyBangumiScheduleRuleEditorPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiScheduleRuleEditor(
            rules = emptyList(),
            onAddRule = {},
            onDeleteRule = {},
            onEpisodeChanged = { _, _ -> },
            onRuleTypeChanged = { _, _ -> },
            onDelayWeeksChanged = { _, _ -> },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NullBangumiScheduleRuleEditorPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiScheduleRuleEditor(
            rules = null,
            onAddRule = {},
            onDeleteRule = {},
            onEpisodeChanged = { _, _ -> },
            onRuleTypeChanged = { _, _ -> },
            onDelayWeeksChanged = { _, _ -> },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BangumiScheduleRuleEditorPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiScheduleRuleEditor(
            rules = listOf(
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
                EpisodeBroadcastRuleUiState(
                    rowId = 3,
                ),
            ),
            onAddRule = {},
            onDeleteRule = {},
            onEpisodeChanged = { _, _ -> },
            onRuleTypeChanged = { _, _ -> },
            onDelayWeeksChanged = { _, _ -> },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorBangumiScheduleRuleEditorPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiScheduleRuleEditor(
            rules = listOf(
                EpisodeBroadcastRuleUiState(
                    rowId = 1,
                    episodeInput = "5",
                    ruleType = EpisodeBroadcastRuleType.DELAY,
                    delayWeeksInput = "1",
                    episodeError = "Episode Error",
                    ruleError = "Rule Error",
                    delayWeeksError = "Delay Weeks Error",
                ),
                EpisodeBroadcastRuleUiState(
                    rowId = 2,
                    episodeInput = "8",
                    ruleType = EpisodeBroadcastRuleType.SAME_DAY_AS_PREVIOUS,
                ),
                EpisodeBroadcastRuleUiState(
                    rowId = 3,
                ),
            ),
            onAddRule = {},
            onDeleteRule = {},
            onEpisodeChanged = { _, _ -> },
            onRuleTypeChanged = { _, _ -> },
            onDelayWeeksChanged = { _, _ -> },
            modifier = Modifier.padding(16.dp),
        )
    }
}

private val EPISODE_COLUMN_WIDTH = 78.dp
private val ACTION_COLUMN_WIDTH = 28.dp
private val INPUT_HEIGHT = 56.dp
private val ROW_HORIZONTAL_PADDING = 8.dp
private val DIVIDER_VERTICAL_PADDING = 8.dp

private const val HEADER_HEIGHT_DP = 36
private const val RULE_ROW_HEIGHT_DP = 56
private const val EMPTY_STATE_HEIGHT_DP = 56
private const val ADD_BUTTON_HEIGHT_DP = 48
