package com.copy9029.bangumimanagerreformed.util

import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun Bangumi.latestAiredEpisode(
    schedules: List<BangumiSchedule>,
    today: LocalDate,
): Int {
    if (schedules.isEmpty()) return 0

    val orderedSchedules = schedules.sortedBy { it.episodeId }
    val currentAnchorIndex = orderedSchedules.indexOfLast {
        !it.broadcastDate.isAfter(today)
    }

    if (currentAnchorIndex == -1) return 0

    val currentAnchor = orderedSchedules[currentAnchorIndex]
    val nextAnchor = orderedSchedules.getOrNull(currentAnchorIndex + 1)
    val weeksPassed = ChronoUnit.WEEKS.between(
        currentAnchor.broadcastDate,
        today,
    ).toInt()

    var episode = currentAnchor.episodeId + weeksPassed

    if (nextAnchor != null) {
        episode = minOf(episode, nextAnchor.episodeId - 1)
    }

    if (totalEpisodes != null) {
        episode = minOf(episode, totalEpisodes)
    }

    return maxOf(0, episode)
}


fun Bangumi.latestAiredBroadcastDate(
    schedules: List<BangumiSchedule>,
    today: LocalDate,
): LocalDate? {
    val latestEpisode = latestAiredEpisode(
        schedules = schedules,
        today = today,
    )

    if (latestEpisode <= 0) return null

    return episodeBroadcastDate(
        episodeId = latestEpisode,
        schedules = schedules,
    )
}


fun Bangumi.episodeBroadcastDate(
    episodeId: Int,
    schedules: List<BangumiSchedule>,
): LocalDate? {
    if (episodeId <= 0 || schedules.isEmpty()) return null

    val anchor = schedules
        .filter { it.episodeId <= episodeId }
        .maxByOrNull { it.episodeId }
        ?: return null

    val weeksAfterAnchor = episodeId - anchor.episodeId

    return anchor.broadcastDate.plusWeeks(weeksAfterAnchor.toLong())
}


fun Bangumi.calculateExpectedEndDate(
    schedules: List<BangumiSchedule>,
): LocalDate? {
    val finalEpisodeId = totalEpisodes?.takeIf { it > 0 }
        ?: return null

    return episodeBroadcastDate(
        episodeId = finalEpisodeId,
        schedules = schedules,
    )
}
