package com.copy9029.bangumimanagerreformed.ui.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.INACTIVE_COLOR_LONG
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import java.time.LocalDate


data class BangumiDetailDialogUiState(
    val titleStr: String,
    val seasonYear: Int,
    val seasonMonth: Int,
    val watchProgress: BangumiWatchProgressUiState,
    val themeColorLong: Long,
    val scoreTimesTen: Int?,
    val firstBroadcastDate: LocalDate,
    val isActive: Boolean,
    val inProjectIDInt: Int,
)

fun Bangumi.toDetailDialogUiState(
    schedules: List<BangumiSchedule>,
    themeColorLong: Long,
    today: LocalDate = LocalDate.now(),
): BangumiDetailDialogUiState {
    return BangumiDetailDialogUiState(
        titleStr = title,
        seasonYear = seasonYear,
        seasonMonth = seasonMonth,
        watchProgress = toWatchProgressUiState(
            schedules = schedules,
            today = today,
        ),
        themeColorLong = themeColorLong,
        scoreTimesTen = myScore,
        firstBroadcastDate = firstBroadcastDate,
        isActive = isActive,
        inProjectIDInt = bangumiId,
    )
}


@Composable
fun BangumiDetailDialog(
    uiState: BangumiDetailDialogUiState,

    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(15.dp),
        title = {
            val fontSize = 20.sp
            val lineHeight = 24.sp
            val maxLines = 3

            var isOverflowed by remember(uiState.titleStr) {
                mutableStateOf(false)
            }

            val scrollState = rememberScrollState()
            val density = LocalDensity.current

            val maxHeight = with(density) {
                (lineHeight * maxLines).toDp()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .then(
                        if (isOverflowed) {
                            Modifier.verticalScroll(scrollState)
                        } else {
                            Modifier
                        }
                    ),
            ) {
                Text(
                    text = uiState.titleStr,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    maxLines = if (isOverflowed) Int.MAX_VALUE else maxLines,
                    onTextLayout = { result ->
                        if (!isOverflowed && result.hasVisualOverflow) {
                            isOverflowed = true
                        }
                    },
                )
            }
        },
        text = {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(uiState.watchProgress.displayText())

                Spacer(modifier = Modifier.height(10.dp))

                DetailInfoRow(
                    label = stringResource(R.string.bangumi_detail_season),
                    value = stringResource(
                        R.string.bangumi_detail_season_value,
                        uiState.seasonYear,
                        uiState.seasonMonth,
                    ),
                )

                DetailInfoRow(
                    label = stringResource(R.string.bangumi_detail_first_broadcast_date),
                    value = uiState.firstBroadcastDate.toString(),
                )

                ColorInfoRow(
                    value = if (uiState.isActive) uiState.themeColorLong else INACTIVE_COLOR_LONG,
                )

                DetailInfoRow(
                    label = stringResource(R.string.bangumi_detail_visibility),
                    value = stringResource(
                        if (uiState.isActive) {
                            R.string.bangumi_detail_visible
                        } else {
                            R.string.bangumi_detail_hidden
                        },
                    ),
                )

                DetailInfoRow(
                    label = stringResource(R.string.bangumi_detail_score),
                    value = uiState.scoreTimesTen?.let { score ->
                        (score / 10.0).toString()
                    } ?: stringResource(R.string.bangumi_detail_score_unknown),
                )

                DetailInfoRow(
                    label = stringResource(R.string.bangumi_detail_internal_id),
                    value = uiState.inProjectIDInt.toString(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onEditClick,
            ) {
                Text(stringResource(R.string.action_edit))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp),
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ColorInfoRow(
    value: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(R.string.bangumi_detail_theme_color),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(value)),
            )

            Text(
                text = "0x%08X".format(value),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
        }
    }
}





@Preview
@Composable
private fun PreviewHere() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BangumiDetailDialog(
            uiState = BangumiDetailDialogUiState(
                titleStr = "标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题",
                seasonYear = 2026,
                seasonMonth = 7,
                watchProgress = BangumiWatchProgressUiState(
                    dayOfWeek = 1,
                    latestWatchedEpisode = 0,
                    latestAiredEpisode = 2,
                    totalEpisodes = 12,
                    startDate = LocalDate.of(2026, 7, 1),
                ),
                themeColorLong = 0xFFFF0000,
                scoreTimesTen = null,
                firstBroadcastDate = LocalDate.of(2026, 7, 1),
                isActive = true,
                inProjectIDInt = 114,
            ),

            onEditClick = {},
            onDismissRequest = {},
        )
    }

}
