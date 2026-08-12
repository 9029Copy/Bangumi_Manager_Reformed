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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.INACTIVE_COLOR_LONG
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.util.latestAiredEpisode
import com.copy9029.bangumimanagerreformed.util.buildBangumiWatchProgressText
import com.copy9029.bangumimanagerreformed.util.latestAiredBroadcastDate
import java.time.LocalDate


data class BangumiDetailDialogUiState(
    val titleStr: String,
    val seasonStr: String,           // "yyyy 年 mm 月"
    val watchProgressStr: String,    // "已看完第 x 话 丨 共/更新到第 y 话"
    val themeColorLong: Long,
    val scoreStr: String,            // "10.0"/"未知"
    val firstBroadcastDateStr: String,
    val isActive: Boolean,
    val inProjectIDInt: Int,
)

fun Bangumi.toDetailDialogUiState(
    schedules: List<BangumiSchedule>,
    themeColorLong: Long,
    today: LocalDate = LocalDate.now(),
): BangumiDetailDialogUiState {
    val latestAiredEpisode = latestAiredEpisode(
        schedules = schedules,
        today = today,
    )
    val latestAiredDate = latestAiredBroadcastDate(
        schedules = schedules,
        today = today,
    )

    return BangumiDetailDialogUiState(
        titleStr = title,
        seasonStr = "$seasonYear 年 $seasonMonth 月",
        watchProgressStr = buildBangumiWatchProgressText(
            dayOfWeekInt = latestAiredDate?.dayOfWeek?.value ?: firstBroadcastDate.dayOfWeek.value,
            latestWatchedEpisode = latestWatchedEpisode,
            latestAiredEpisode = latestAiredEpisode,
            totalEpisodes = totalEpisodes,
            startDate = firstBroadcastDate,
        ),
        themeColorLong = themeColorLong,
        scoreStr = myScore?.let { (it / 10.0).toString() } ?: "未知",
        firstBroadcastDateStr = firstBroadcastDate.toString(),
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
                Text(uiState.watchProgressStr)

                Spacer(modifier = Modifier.height(10.dp))

                DetailInfoRow(
                    label = "季度",
                    value = uiState.seasonStr,
                )

                DetailInfoRow(
                    label = "首播日期",
                    value = uiState.firstBroadcastDateStr,
                )

                ColorInfoRow(
                    value = if (uiState.isActive) uiState.themeColorLong else INACTIVE_COLOR_LONG,
                )

                DetailInfoRow(
                    label = "是否隐藏",
                    value = if (uiState.isActive) "正常显示" else "已隐藏"
                )

                DetailInfoRow(
                    label = "评分",
                    value = uiState.scoreStr,
                )

                DetailInfoRow(
                    label = "内部ID",
                    value = uiState.inProjectIDInt.toString(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onEditClick,
            ) {
                Text("编辑")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text("关闭")
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
            text = "主题颜色",
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
                seasonStr = "2026 年 07 月",
                watchProgressStr = "周一 丨 已看完第 0 话 丨 更新到第 2 话",
                themeColorLong = 0xFFFF0000,
                scoreStr = "未知",
                firstBroadcastDateStr = "2026-07-01",
                isActive = true,
                inProjectIDInt = 114,
            ),

            onEditClick = {},
            onDismissRequest = {},
        )
    }

}
