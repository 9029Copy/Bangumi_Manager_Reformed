package com.copy9029.bangumimanagerreformed.util

import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import java.time.LocalDate


fun buildBangumiWatchProgressText(
    dayOfWeekInt: Int,
    latestWatchedEpisode: Int,
    latestAiredEpisode: Int,
    totalEpisodes: Int?,
    startDate: LocalDate,
): String {
    val str0 = when (dayOfWeekInt) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> "周？"
    }
    val str1 = if (latestWatchedEpisode > 0/*TODO: 第0话？*/) {
        "已看完第 $latestWatchedEpisode 话"
    } else {
        "尚未观看"
    }

    val str2 = if (latestAiredEpisode > 0) {
        if (totalEpisodes == null || latestAiredEpisode < totalEpisodes) {
            "更新到第 $latestAiredEpisode 话"
        } else {
            "共 $totalEpisodes 话"
        }
    } else {
        "${startDate.month} 月 ${startDate.dayOfMonth} 日开播"
    }

    return "$str0 丨 $str1 丨 $str2"
}


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
    val weeksPassed = java.time.temporal.ChronoUnit.WEEKS.between(
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
