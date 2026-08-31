package com.copy9029.bangumimanagerreformed.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.theme.generateBangumiColorScheme

@Composable
internal fun CalendarSelectedDateDetails(
    bangumis: List<CalendarBangumiItemUiState>,
    onAddClick: () -> Unit,
    onBangumiClick: (Int) -> Unit,
    onMarkEpisodeDoneClick: (Int, Int) -> Unit,
    onMarkEpisodeUndoneClick: (Int, Int) -> Unit,
    onToggleBangumiActiveClick: (Int) -> Unit,
    onDeleteBangumiClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedMoreMenuBangumiId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    var pendingSetActiveBangumi by remember {
        mutableStateOf<CalendarBangumiItemUiState?>(null)
    }
    var pendingDeleteBangumi by remember {
        mutableStateOf<CalendarBangumiItemUiState?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        bangumis.forEach { bangumi ->
            CalendarSelectedDateDetailCard(
                item = bangumi,
                isMoreMenuExpanded = expandedMoreMenuBangumiId == bangumi.bangumiId,
                onClick = {
                    onBangumiClick(bangumi.bangumiId)
                },
                onDoneButtonClick = {
                    if (bangumi.isDone) {
                        onMarkEpisodeUndoneClick(bangumi.bangumiId, bangumi.episodeId)
                    } else {
                        onMarkEpisodeDoneClick(bangumi.bangumiId, bangumi.episodeId)
                    }
                },
                onEditClick = {
                    onEditClick(bangumi.bangumiId)
                },
                onMoreClick = {
                    expandedMoreMenuBangumiId = bangumi.bangumiId
                },
                onDismissMoreMenu = {
                    expandedMoreMenuBangumiId = null
                },
                onToggleActiveClick = {
                    expandedMoreMenuBangumiId = null
                    pendingSetActiveBangumi = bangumi
                },
                onDeleteClick = {
                    expandedMoreMenuBangumiId = null
                    pendingDeleteBangumi = bangumi
                },
            )
        }

        Card(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 30.dp),
            shape = RoundedCornerShape(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.action_add_item),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    pendingSetActiveBangumi?.let { bangumi ->
        val confirmationTitle = stringResource(
            if (bangumi.isActive) R.string.item_confirm_hide else R.string.item_confirm_unhide,
        )
        AlertDialog(
            onDismissRequest = {
                pendingSetActiveBangumi = null
            },
            title = {
                Text(confirmationTitle)
            },
            text = {
                Text(bangumi.title)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingSetActiveBangumi = null
                        onToggleBangumiActiveClick(bangumi.bangumiId)
                    },
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingSetActiveBangumi = null
                    },
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    pendingDeleteBangumi?.let { bangumi ->
        AlertDialog(
            onDismissRequest = {
                pendingDeleteBangumi = null
            },
            title = {
                Text(stringResource(R.string.item_confirm_delete))
            },
            text = {
                Text(bangumi.title)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteBangumi = null
                        onDeleteBangumiClick(bangumi.bangumiId)
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingDeleteBangumi = null
                    },
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun CalendarSelectedDateDetailCard(
    item: CalendarBangumiItemUiState,
    isMoreMenuExpanded: Boolean,
    onClick: () -> Unit,
    onDoneButtonClick: () -> Unit,
    onEditClick: () -> Unit,
    onMoreClick: () -> Unit,
    onDismissMoreMenu: () -> Unit,
    onToggleActiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(item.themeColorLong)
    val titleColor = if (item.isDone) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val episodeColor = if (item.isDone) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = onDoneButtonClick,
                ) {
                    if (item.isDone) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.item_completed),
                            modifier = Modifier.size(24.dp),
                            tint = colorScheme.main,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.calendar_outline_circle),
                            contentDescription = stringResource(R.string.item_not_completed),
                            modifier = Modifier.size(22.dp),
                            tint = colorScheme.main,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = item.title,
                        color = titleColor,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = stringResource(R.string.calendar_episode_number, item.episodeId),
                        color = episodeColor,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalendarCircleActionButton(
                    onClick = onEditClick,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Box {
                    CalendarCircleActionButton(
                        onClick = onMoreClick,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.action_more),
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    DropdownMenu(
                        expanded = isMoreMenuExpanded,
                        onDismissRequest = onDismissMoreMenu,
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        if (item.isActive) {
                                            R.string.action_hide
                                        } else {
                                            R.string.action_unhide
                                        },
                                    ),
                                )
                            },
                            onClick = onToggleActiveClick,
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete)) },
                            onClick = onDeleteClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCircleActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    content: @Composable () -> Unit,
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = modifier.size(30.dp),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor,
        ),
        colors = colors,
        shape = CircleShape,
        content = content,
    )
}

