package com.copy9029.bangumimanagerreformed.ui.index

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme


enum class SortTags(@param:StringRes val labelRes: Int) {
    FOCUSING_UPDATE_MODE(R.string.index_sort_by_completion),
    BY_MODIFIED_ORDER(R.string.index_sort_by_modified),
    BY_RECENT_UPDATE(R.string.index_sort_by_recent_update),
    BY_START_TIME(R.string.index_sort_by_start_date),
}

enum class SortOrders {
    ASC,
    DESC,
}


@Composable
fun SortAndFilterRow(
    isFocusingUpdating: Boolean,
    selectedSortTag: SortTags,
    selectedSortOrder: SortOrders,

    onFocusingUpdatingChanged: (Boolean) -> Unit,
    onSortTagSelected: (SortTags) -> Unit,
    onSortOrderSelected: (SortOrders) -> Unit,

    onOpenMoreFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = isFocusingUpdating,
                onClick = { onFocusingUpdatingChanged(!isFocusingUpdating) },
                label = {
                    Text(stringResource(R.string.index_filter_following_mode))
                },
                leadingIcon = {
                    Checkbox(
                        checked = isFocusingUpdating,
                        onCheckedChange = onFocusingUpdatingChanged,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
            )

            FilterChip(
                selected = false,
                onClick = onOpenMoreFilters,
                label = {
                    Text(stringResource(R.string.index_filter_title))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.filter_alt_outlined),
                        contentDescription = stringResource(R.string.index_filter_more),
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 右侧排序区域：固定靠右
        SortControl(
            selectedSortTag = selectedSortTag,
            selectedSortOrder = selectedSortOrder,
            onSortTagSelected = onSortTagSelected,
            onSortOrderSelected = onSortOrderSelected,
        )
    }
}

@Composable
private fun SortControl(
    selectedSortTag: SortTags,
    selectedSortOrder: SortOrders,
    onSortTagSelected: (SortTags) -> Unit,
    onSortOrderSelected: (SortOrders) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            AssistChip(
                onClick = { expanded = true },
                label = {
                    Text(
                        text = stringResource(selectedSortTag.labelRes),
                        maxLines = 1,
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = when (selectedSortOrder) {
                            SortOrders.ASC -> painterResource(R.drawable.sort_arrow_upward)
                            SortOrders.DESC -> painterResource(R.drawable.sort_arrow_downward)
                        },
                        contentDescription = when (selectedSortOrder) {
                            SortOrders.ASC -> stringResource(
                                R.string.index_sort_ascending_toggle,
                            )
                            SortOrders.DESC -> stringResource(
                                R.string.index_sort_descending_toggle,
                            )
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                val newOrder = when (selectedSortOrder) {
                                    SortOrders.ASC -> SortOrders.DESC
                                    SortOrders.DESC -> SortOrders.ASC
                                }
                                onSortOrderSelected(newOrder)
                            },
                    )
                },
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                SortTags.entries.forEach { sortTag ->
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(sortTag.labelRes))
                        },
                        onClick = {
                            onSortTagSelected(sortTag)
                            expanded = false
                        },
                        trailingIcon = {
                            if (sortTag == selectedSortTag) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = stringResource(R.string.index_sort_selected),
                                )
                            }
                        },
                    )
                }
            }
        }

    }
}






@Preview
@Composable
private fun PreviewHere() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) {
            SortAndFilterRow(
                isFocusingUpdating = false,
                selectedSortTag = SortTags.BY_RECENT_UPDATE,
                selectedSortOrder = SortOrders.ASC,

                onFocusingUpdatingChanged = {},
                onSortTagSelected = {},
                onSortOrderSelected = {},

                onOpenMoreFilters = {},
                modifier = Modifier,
            )
        }
    }

}
