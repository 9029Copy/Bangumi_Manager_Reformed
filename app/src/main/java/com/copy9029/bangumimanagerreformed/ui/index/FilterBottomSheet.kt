package com.copy9029.bangumimanagerreformed.ui.index

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme


enum class WatchedTags(@param:StringRes val labelRes: Int) {
    ALL(R.string.index_filter_all),
    UNFINISHED(R.string.index_filter_unfinished),
    FINISHED(R.string.index_filter_finished),
}

enum class InactiveTags(@param:StringRes val labelRes: Int) {
    ACTIVE(R.string.index_filter_inactive_hidden),
    ALL(R.string.index_filter_inactive_all),
    INACTIVE(R.string.index_filter_inactive_only),
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
            Text(text = stringResource(R.string.index_filter_title), fontSize = 20.sp)

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

            FilterSection2(title = stringResource(R.string.index_filter_end_status)) {
                WatchedTags.entries.forEach { tag ->
                    FilterChip(
                        selected = status.watchedTag == tag,
                        onClick = {
                            onStatusChange(status.copy(watchedTag = tag))
                        },
                        label = {
                            Text(text = stringResource(tag.labelRes), fontSize = 12.sp)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            FilterSection2(title = stringResource(R.string.index_filter_hidden_items)) {
                InactiveTags.entries.forEach { tag ->
                    FilterChip(
                        selected = status.inactiveTag == tag,
                        onClick = {
                            onStatusChange(status.copy(inactiveTag = tag))
                        },
                        label = {
                            Text(text = stringResource(tag.labelRes), fontSize = 12.sp)
                        },
                        modifier = Modifier.weight(1f)
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
                                watchedTag = WatchedTags.ALL,
                                inactiveTag = InactiveTags.ACTIVE,
                            )
                        )
                    },
                ) {
                    Text(stringResource(R.string.action_reset))
                }

                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text(stringResource(R.string.action_done))
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
    val allLabel = stringResource(R.string.index_filter_all)
    val yearOptions = buildList<FilterDropdownOption<Int?>> {
        add(FilterDropdownOption(null, allLabel))

        if (startYear <= endYear) {
            for (year in endYear downTo startYear) {
                add(FilterDropdownOption(year, year.toString()))
            }
        }
    }

    val monthOptions = listOf<FilterDropdownOption<Int?>>(
        FilterDropdownOption(null, allLabel),
        FilterDropdownOption(1, stringResource(R.string.index_filter_month, 1)),
        FilterDropdownOption(4, stringResource(R.string.index_filter_month, 4)),
        FilterDropdownOption(7, stringResource(R.string.index_filter_month, 7)),
        FilterDropdownOption(10, stringResource(R.string.index_filter_month, 10)),
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = stringResource(R.string.index_filter_quarter), fontSize = 16.sp)

        Spacer(modifier = Modifier.width(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilterDropdown(
                selectedValue = selectedYear,
                options = yearOptions,
                onValueSelected = onYearSelected,
                modifier = Modifier.weight(1.1f),
            )

            FilterDropdown(
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, fontSize = 16.sp)

        Spacer(modifier = Modifier.width(30.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
//            verticalArrangement = Arrangement.spacedBy(8.dp),
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
        ?: stringResource(R.string.index_filter_all)

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
                Text(text = selectedText, fontSize = 12.sp)
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
                        Text(option.label, fontSize = 12.sp, lineHeight = 18.sp,)
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
    BangumiManagerReformedTheme(dynamicColor = false) {
        FilterBottomSheet(
            status = SortAndFilterStatus(
                null,
                null,
                WatchedTags.ALL,
                InactiveTags.ACTIVE,
                SortTags.FOCUSING_UPDATE_MODE,
                SortOrders.ASC
            ),
            onStatusChange = {},
            onDismissRequest = {},
            startYear = 2025,
            endYear = 2026,
        )
    }

}
