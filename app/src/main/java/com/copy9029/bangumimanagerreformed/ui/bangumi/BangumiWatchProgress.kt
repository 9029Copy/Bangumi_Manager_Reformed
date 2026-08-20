package com.copy9029.bangumimanagerreformed.ui.bangumi

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.util.latestAiredBroadcastDate
import com.copy9029.bangumimanagerreformed.util.latestAiredEpisode
import java.time.LocalDate

data class BangumiWatchProgressUiState(
    val dayOfWeek: Int,
    val latestWatchedEpisode: Int,
    val latestAiredEpisode: Int,
    val totalEpisodes: Int?,
    val startDate: LocalDate,
)

fun Bangumi.toWatchProgressUiState(
    schedules: List<BangumiSchedule>,
    today: LocalDate = LocalDate.now(),
): BangumiWatchProgressUiState {
    val latestAiredEpisode = latestAiredEpisode(
        schedules = schedules,
        today = today,
    )
    val latestAiredDate = latestAiredBroadcastDate(
        schedules = schedules,
        today = today,
    )

    return BangumiWatchProgressUiState(
        dayOfWeek = latestAiredDate?.dayOfWeek?.value ?: firstBroadcastDate.dayOfWeek.value,
        latestWatchedEpisode = latestWatchedEpisode,
        latestAiredEpisode = latestAiredEpisode,
        totalEpisodes = totalEpisodes,
        startDate = firstBroadcastDate,
    )
}

@Composable
fun BangumiWatchProgressUiState.displayText(): String {
    val weekdayLabels = stringArrayResource(R.array.weekday_labels_monday_first)
    val weekday = weekdayLabels.getOrNull(dayOfWeek - 1)?.let { label ->
        stringResource(R.string.schedule_weekday, label)
    } ?: stringResource(R.string.schedule_weekday_unknown)

    val watchedProgress = if (latestWatchedEpisode > 0) {
        pluralStringResource(
            R.plurals.schedule_watched_episode,
            latestWatchedEpisode,
            latestWatchedEpisode,
        )
    } else {
        stringResource(R.string.schedule_not_watched)
    }

    val broadcastProgress = when {
        latestAiredEpisode <= 0 -> stringResource(
            R.string.schedule_start_date,
            startDate.monthValue,
            startDate.dayOfMonth,
        )
        totalEpisodes == null || latestAiredEpisode < totalEpisodes -> pluralStringResource(
            R.plurals.schedule_latest_aired_episode,
            latestAiredEpisode,
            latestAiredEpisode,
        )
        else -> pluralStringResource(
            R.plurals.schedule_total_episodes,
            totalEpisodes,
            totalEpisodes,
        )
    }

    return stringResource(
        R.string.schedule_progress_summary,
        weekday,
        watchedProgress,
        broadcastProgress,
    )
}
