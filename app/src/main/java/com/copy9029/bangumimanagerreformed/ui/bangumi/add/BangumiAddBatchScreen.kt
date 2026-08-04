package com.copy9029.bangumimanagerreformed.ui.bangumi.add

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.ui.MondayFirstCalendarLocale
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BangumiAddBatchScreen(
    viewModel: AddBatchViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BangumiAddBatchScreenContent(
        uiState = uiState,
        onBack = onBack,
        onSubmitAllClick = {
            val msg = viewModel.onSubmitAllClick()
            if (msg[0] == 'T') {
                Toast.makeText(context, msg.substring(startIndex = 1), Toast.LENGTH_SHORT).show()
                onBack()
            } else {
                Toast.makeText(context, msg.substring(startIndex = 1), Toast.LENGTH_SHORT).show()
            }
        },
        onSeasonChanged = viewModel::onSeasonChanged,
        onFormattedTextChanged = viewModel::onFormattedTextChanged,
        onParseClick = {
            val result = viewModel.onParseClick()
            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        },
        onTitleChanged = viewModel::onTitleChanged,
        onFirstBroadcastDateChanged = viewModel::onFirstBroadcastDateChanged,
        onDeleteClick = viewModel::onRequestDeleteItem,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onConfirmDelete = viewModel::onConfirmDeleteItem,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BangumiAddBatchScreenContent(
    uiState: AddBatchUiState,
    onBack: () -> Unit,
    onSubmitAllClick: () -> Unit,
    onSeasonChanged: (Int, Int) -> Unit,
    onFormattedTextChanged: (String) -> Unit,
    onParseClick: () -> Unit,
    onTitleChanged: (Long, String) -> Unit,
    onFirstBroadcastDateChanged: (Long, LocalDate) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("批量添加") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = onSubmitAllClick) {
                        Text("提交全部")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Box(
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 8.dp,
                ),
            ) {
                SeasonSelectSection(
                    startYear = uiState.startYear,
                    endYear = uiState.endYear,
                    selectedYear = uiState.seasonYear,
                    selectedMonth = uiState.seasonMonth,
                    onSeasonSelected = onSeasonChanged,
                    colorLong = uiState.themeColorLong,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 4.dp,
                    end = 16.dp,
                    bottom = 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = uiState.formattedText,
                            onValueChange = onFormattedTextChanged,
                            label = { Text("格式化文本") },
                            modifier = Modifier.weight(1f),
                            minLines = 3,
                            maxLines = Int.MAX_VALUE,
                        )

                        TextButton(onClick = onParseClick) {
                            Text(text = "解析")
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }

                if (uiState.items.isEmpty()) {
                    item {
                        Text(
                            text = "解析后的项目会显示在这里",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    item {
                        BatchAddListHeader()
                    }

                    items(
                        items = uiState.items,
                        key = { it.id },
                    ) { item ->
                        BatchAddItemCard(
                            item = item,
                            onTitleChanged = { onTitleChanged(item.id, it) },
                            onFirstBroadcastDateChanged = {
                                onFirstBroadcastDateChanged(item.id, it)
                            },
                            onDeleteClick = { onDeleteClick(item.id) },
                        )
                    }
                }
            }
        }
    }

    uiState.pendingDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text("删除这一项？") },
            text = {
                Text(
                    text = item.title.ifBlank { "未命名项目" },
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun BatchAddListHeader(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "标题",
            modifier = Modifier.weight(1.8f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = "开播日期",
            modifier = Modifier
                .widthIn(min = 120.dp)
                .weight(1f)
                .padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun BatchAddItemCard(
    item: BatchAddItemUiState,
    onTitleChanged: (String) -> Unit,
    onFirstBroadcastDateChanged: (LocalDate) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = item.title,
                onValueChange = onTitleChanged,
                modifier = Modifier
                    .weight(1.8f)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                minLines = 1,
                maxLines = 6,
                decorationBox = { innerTextField ->
                    Box {
                        if (item.title.isBlank()) {
                            Text(
                                text = "点击输入标题",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            BatchFirstBroadcastDateCell(
                date = item.firstBroadcastDate,
                onDateSelected = onFirstBroadcastDateChanged,
                modifier = Modifier
                    .widthIn(min = 120.dp)
                    .weight(1f)
                    .fillMaxHeight(),
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "删除")
            }
        }

        item.titleError?.let { error ->
            HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
            Text(
                text = error,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchFirstBroadcastDateCell(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clickable { showDatePicker = true }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = date.toString(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
    }

    if (showDatePicker) {
        MondayFirstCalendarLocale {
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
                            datePickerState.selectedDateMillis
                                ?.let { millis ->
                                    Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                }
                                ?.let(onDateSelected)
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
}


@Preview
@Composable
private fun PreviewBangumiAddBatchScreen() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiAddBatchScreenContent(
            uiState = AddBatchUiState(
                seasonYear = 2026,
                seasonMonth = 7,
                formattedText = "标题A\n2026年7月1日\n标题B\n2026年7月8日",
                startYear = 2021,
                endYear = 2028,
                items = List(5) { index ->
                    BatchAddItemUiState(
                        id = index.toLong(),
                        title = "示例标题 ${index + 1}".repeat(index + 2),
                        firstBroadcastDate = LocalDate.of(2026, 7, 1).plusWeeks(index.toLong()),
                        titleError = if (index == 4) "error message" else null,
                    )
                },
            ),
            onBack = {},
            onSubmitAllClick = {},
            onSeasonChanged = { _, _ -> },
            onFormattedTextChanged = {},
            onParseClick = {},
            onTitleChanged = { _, _ -> },
            onFirstBroadcastDateChanged = { _, _ -> },
            onDeleteClick = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
        )
    }
}
