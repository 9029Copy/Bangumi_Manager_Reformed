package com.copy9029.bangumimanagerreformed.ui.bangumi.add

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.ui.components.MyDatePickerDialog
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import java.time.LocalDate


@Composable
fun BangumiAddSheet(
    viewModel: AddSheetViewModel,
    defaultFirstBroadcastDate: LocalDate,
    onDismissRequest: () -> Unit,
    onBatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(defaultFirstBroadcastDate) {
        viewModel.initializeForOpen(defaultFirstBroadcastDate)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val thisContext = LocalContext.current

    uiState?.let { state ->
        BangumiAddSheetContent(
            uiState = state,
            onSeasonChanged = viewModel::onSeasonChanged,
            onTitleChanged = viewModel::onTitleChanged,
            onFirstBroadcastDateChanged = viewModel::onFirstBroadcastDateChanged,
            onBatchClick = onBatchClick,
            onDismissRequest = onDismissRequest,
            onConfirmClick = {
                if (viewModel.onConfirmClick(defaultFirstBroadcastDate)) {
                    onDismissRequest()
                    Toast.makeText(thisContext, "添加成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(thisContext, "添加失败", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BangumiAddSheetContent(
    uiState: BangumiAddSheetUiState,
    onSeasonChanged: (year: Int, month: Int) -> Unit,
    onTitleChanged: (String) -> Unit,
    onFirstBroadcastDateChanged: (LocalDate) -> Unit,
    onBatchClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "添加项目", fontSize = 20.sp)

                TextButton(onClick = onBatchClick) {
                    Text(text = "批量添加", fontSize = 16.sp)
                }
            }

            // 季度选择部分
            SeasonSelectSection(
                startYear = uiState.startYear,
                endYear = uiState.endYear,
                selectedYear = uiState.seasonYear,
                selectedMonth = uiState.seasonMonth,
                onSeasonSelected = onSeasonChanged,
                colorLong = uiState.themeColorLong
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "标题",
                    modifier = Modifier.width(80.dp),
                    fontSize = 16.sp,
                )

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle.Default.copy(fontSize = 16.sp),
                    minLines = 2,
                    isError = uiState.titleError != null,
                    supportingText = uiState.titleError?.let { error ->
                        { Text(error) }
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "开播日期",
                    modifier = Modifier.width(80.dp),
                    fontSize = 16.sp,
                )

                FirstBroadcastDateField(
                    date = uiState.firstBroadcastDate,
                    onDateSelected = onFirstBroadcastDateChanged,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("取消")
                }

                TextButton(onClick = onConfirmClick) {
                    Text("提交")
                }
            }
        }
    }
}


@Composable
fun SeasonSelectSection(
    startYear: Int,
    endYear: Int,
    selectedYear: Int,
    selectedMonth: Int,
    onSeasonSelected: (Int, Int) -> Unit,
    colorLong: Long,
) {
    val yearOptions = buildList<SeasonDropdownOption<Int>> {
        if (startYear <= endYear) {
            for (year in endYear downTo startYear) {
                add(SeasonDropdownOption(year, year.toString() + ""))
            }
        }
    }

    val monthOptions = listOf(
        SeasonDropdownOption(1, "1月"),
        SeasonDropdownOption(4, "4月"),
        SeasonDropdownOption(7, "7月"),
        SeasonDropdownOption(10, "10月"),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = "季度", fontSize = 16.sp)

        Spacer(modifier = Modifier.width(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SeasonDropdown(
                selectedValue = selectedYear,
                options = yearOptions,
                onValueSelected = { year ->
                    onSeasonSelected(year, selectedMonth)
                },
                modifier = Modifier.width(100.dp),
            )

            SeasonDropdown(
                selectedValue = selectedMonth,
                options = monthOptions,
                onValueSelected = { month ->
                    onSeasonSelected(selectedYear, month)
                },
                modifier = Modifier.width(100.dp),
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(colorLong)),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstBroadcastDateField(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = date.toString(),
            textStyle = TextStyle.Default.copy(
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,

        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    showDatePicker = true
                }
        )
    }



    if (showDatePicker) {
        MyDatePickerDialog(
            initialDate = date,
            onDateSelected = onDateSelected,
            onDismissRequest = {
                showDatePicker = false
            },
        )
    }
}

private data class SeasonDropdownOption<T>(
    val value: T,
    val label: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SeasonDropdown(
    selectedValue: T,
    options: List<SeasonDropdownOption<T>>,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedText = options
        .firstOrNull { it.value == selectedValue }
        ?.label
        ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        AssistChip(
            modifier = Modifier
                .width(130.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            onClick = { expanded = true },
            label = {
                Text(selectedText)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 240.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.height(36.dp),
                    text = {
                        Text(option.label, fontSize = 14.sp, lineHeight = 18.sp)
                    },
                    onClick = {
                        onValueSelected(option.value)
                        expanded = false
                    },
                    contentPadding = PaddingValues(
                        horizontal = 10.dp,
                        vertical = 2.dp,
                    ),
                )
            }
        }
    }
}


@Preview
@Composable
private fun PreviewHere() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiAddSheetContent(
            uiState = BangumiAddSheetUiState(
                seasonYear = 2026,
                seasonMonth = 1,
                title = "踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩",
                firstBroadcastDate = LocalDate.of(2026, 7, 19),
                startYear = 2021,
                endYear = 2028,
            ),
            onSeasonChanged = { _, _ -> },
            onTitleChanged = {},
            onFirstBroadcastDateChanged = {},
            onBatchClick = {},
            onDismissRequest = {},
            onConfirmClick = {},
        )
    }
}
