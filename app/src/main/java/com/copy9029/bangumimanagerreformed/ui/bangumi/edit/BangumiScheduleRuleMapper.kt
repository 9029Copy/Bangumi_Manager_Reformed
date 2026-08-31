package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 根据当前（数据库中）的Schedules推算RuleList，若推算失败则返回null
 */
internal fun List<BangumiSchedule>.toRuleList(
    firstBroadcastDate: LocalDate,
): List<EpisodeBroadcastRuleUiState>? {
    if (isEmpty()) return null

    val orderedSchedules = sortedBy(BangumiSchedule::episodeId)
    val firstSchedule = orderedSchedules.first()
    if (
        firstSchedule.episodeId != 1 ||
        firstSchedule.broadcastDate != firstBroadcastDate ||
        orderedSchedules.any { it.bangumiId != firstSchedule.bangumiId } ||
        orderedSchedules.zipWithNext().any { (previous, current) ->
            previous.episodeId == current.episodeId
        }
    ) {
        return null
    }

    val rules = mutableListOf<EpisodeBroadcastRuleUiState>()
    orderedSchedules.zipWithNext().forEach { (previous, current) ->
        val normalBroadcastDate = previous.broadcastDate.plusWeeks(
            (current.episodeId - previous.episodeId).toLong()
        )
        val offsetDays = ChronoUnit.DAYS.between(
            normalBroadcastDate,
            current.broadcastDate,
        )
        if (offsetDays % 7L != 0L) return null

        val offsetWeeks = offsetDays / 7L
        val rule = when {
            offsetWeeks == 0L -> null
            offsetWeeks == -1L -> EpisodeBroadcastRuleUiState(
                rowId = rules.size + 1L,
                episodeInput = current.episodeId.toString(),
                ruleType = EpisodeBroadcastRuleType.SAME_DAY_AS_PREVIOUS,
            )
            offsetWeeks in 1L..Int.MAX_VALUE.toLong() -> EpisodeBroadcastRuleUiState(
                rowId = rules.size + 1L,
                episodeInput = current.episodeId.toString(),
                ruleType = EpisodeBroadcastRuleType.DELAY,
                delayWeeksInput = offsetWeeks.toString(),
            )
            else -> return null
        }

        rule?.let(rules::add)
    }

    return rules
}

/**
 * 根据编辑后的RuleList推算Schedules（并用于更新数据库）
 */
internal fun List<EpisodeBroadcastRuleUiState>.toScheduleList(
    bangumiId: Int,
    firstBroadcastDate: LocalDate,
): List<BangumiSchedule> {
    val schedules = mutableListOf(
        BangumiSchedule(
            bangumiId = bangumiId,
            episodeId = 1,
            broadcastDate = firstBroadcastDate,
        )
    )

    sortedBy { rule -> requireNotNull(rule.episodeInput.toIntOrNull()) }
        .forEach { rule ->
            val episodeId = requireNotNull(rule.episodeInput.toIntOrNull())
            require(episodeId > 1) {
                "播出规则只能应用于第 2 集及之后的集数"
            }

            val previousAnchor = schedules.last()
            val normalBroadcastDate = previousAnchor.broadcastDate.plusWeeks(
                (episodeId - previousAnchor.episodeId).toLong()
            )
            val broadcastDate = when (requireNotNull(rule.ruleType)) {
                EpisodeBroadcastRuleType.DELAY -> normalBroadcastDate.plusWeeks(
                    requireNotNull(rule.delayWeeksInput.toLongOrNull())
                )
                EpisodeBroadcastRuleType.SAME_DAY_AS_PREVIOUS ->
                    normalBroadcastDate.minusWeeks(1L)
            }

            schedules += BangumiSchedule(
                bangumiId = bangumiId,
                episodeId = episodeId,
                broadcastDate = broadcastDate,
            )
        }

    return schedules
}

