package com.copy9029.bangumimanagerreformed.ui.index

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class UpdateTags(val label: String) {
    ALL("全部"),
    UPDATING("连载中"),
    ENDED("已完结"),
}

enum class WatchedTags(val label: String) {
    ALL("全部"),
    UNFINISHED("未看完"),
    FINISHED("已看完"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    status: SortAndFilterStatus,
    onStatusChange: (SortAndFilterStatus) -> Unit,
    onDismissRequest: () -> Unit,
    startYear: Int,
    endYear: Int,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "筛选", fontSize = 24.sp)

            Spacer(modifier = Modifier.height(3.dp))

            FilterSection1(
                startYear = startYear,
                endYear = endYear,
                selectedYear = status.seasonYear,
                selectedMonth = status.seasonMonth,
                onYearSelected = { year ->
                    onStatusChange(status.copy(seasonYear = year))
                },
                onMonthSelected = { month ->
                    onStatusChange(status.copy(seasonMonth = month))
                },
            )

            FilterSection2(title = "更新状态") {
                UpdateTags.entries.forEach { tag ->
                    FilterChip(
                        selected = status.updateTag == tag,
                        onClick = {
                            onStatusChange(status.copy(updateTag = tag))
                        },
                        label = {
                            Text(tag.label)
                        },
                    )
                }
            }

            FilterSection2(title = "观看状态") {
                WatchedTags.entries.forEach { tag ->
                    FilterChip(
                        selected = status.watchedTag == tag,
                        onClick = {
                            onStatusChange(status.copy(watchedTag = tag))
                        },
                        label = {
                            Text(tag.label)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = {
                        onStatusChange(
                            status.copy(
                                seasonYear = null,
                                seasonMonth = null,
                                updateTag = UpdateTags.ALL,
                                watchedTag = WatchedTags.ALL,
                            )
                        )
                    },
                ) {
                    Text("重置")
                }

                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text("完成")
                }
            }
        }
    }
}

@Composable
private fun FilterSection1(
    startYear: Int,
    endYear: Int,
    selectedYear: Int?,
    selectedMonth: Int?,
    onYearSelected: (Int?) -> Unit,
    onMonthSelected: (Int?) -> Unit,
) {
    val yearOptions = buildList<FilterDropdownOption<Int?>> {
        add(FilterDropdownOption(null, "全部"))

        if (startYear <= endYear) {
            for (year in endYear downTo startYear) {
                add(FilterDropdownOption(year, year.toString()))
            }
        }
    }

    val monthOptions = listOf<FilterDropdownOption<Int?>>(
        FilterDropdownOption(null, "全部"),
        FilterDropdownOption(1, "1月"),
        FilterDropdownOption(4, "4月"),
        FilterDropdownOption(7, "7月"),
        FilterDropdownOption(10, "10月"),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "季度", fontSize = 18.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilterDropdown(
                label = "年份",
                selectedValue = selectedYear,
                options = yearOptions,
                onValueSelected = onYearSelected,
                modifier = Modifier.weight(1f),
            )

            FilterDropdown(
                label = "月份",
                selectedValue = selectedMonth,
                options = monthOptions,
                onValueSelected = onMonthSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FilterSection2(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, fontSize = 18.sp)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}


private data class FilterDropdownOption<T>(
    val value: T,
    val label: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterDropdown(
    label: String,
    selectedValue: T,
    options: List<FilterDropdownOption<T>>,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedText = options
        .firstOrNull { it.value == selectedValue }
        ?.label
        ?: "全部"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier,
    ) {
        AssistChip(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            onClick = {
                expanded = true
            },
            label = {
                Text("$label：$selectedText")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier
                .heightIn(max = 240.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.height(36.dp),
                    text = {
                        Text(option.label, fontSize = 14.sp, lineHeight = 18.sp,)
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
private fun PreviewBottomSheet() {
    FilterBottomSheet(
        status = SortAndFilterStatus(
            null,
            null,
            UpdateTags.ALL,
            WatchedTags.ALL,
            SortTags.BY_START_TIME,
            SortOrders.ASC
        ),
        onStatusChange = {},
        onDismissRequest = {},
        startYear = 2025,
        endYear = 2026,
    )
}