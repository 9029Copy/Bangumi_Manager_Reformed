package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import com.copy9029.bangumimanagerreformed.R

internal fun validateTitle(input: String): Int? {
    return if (input.isBlank()) R.string.bangumi_error_title_required else null
}

internal fun validateMyScore(input: String): Int? {
    if (input.isBlank()) return null
    if (!input.matches(Regex("""\d+(?:\.\d+)?"""))) {
        return R.string.bangumi_edit_error_score_characters
    }

    val scoreTimesTen = try {
        input.toBigDecimal()
            .movePointRight(1)
            .intValueExact()
    } catch (_: ArithmeticException) {
        return R.string.bangumi_edit_error_score_decimal_places
    } catch (_: NumberFormatException) {
        return R.string.bangumi_edit_error_score_format
    }

    return if (scoreTimesTen in 0..100) {
        null
    } else {
        R.string.bangumi_edit_error_score_range
    }
}

internal fun validateTotalEpisodes(input: String): Int? {
    if (input.isBlank()) return null

    val totalEpisodes = input.toIntOrNull()
    return if (totalEpisodes != null && totalEpisodes > 0) {
        null
    } else {
        R.string.bangumi_edit_error_total_episodes_positive
    }
}

internal fun validateLatestWatchedEpisode(
    input: String,
    totalEpisodes: Int?,
): Int? {
    val latestWatchedEpisode = input.toIntOrNull()
        ?: return R.string.bangumi_edit_error_watched_episode_nonnegative_integer

    if (latestWatchedEpisode < 0) {
        return R.string.bangumi_edit_error_watched_episode_nonnegative
    }
    if (totalEpisodes != null && latestWatchedEpisode > totalEpisodes) {
        return R.string.bangumi_edit_error_watched_episode_exceeds_total
    }

    return null
}

internal fun validateRuleEpisodes(
    rules: List<EpisodeBroadcastRuleUiState>,
    targetRowId: Long,
    totalEpisodes: Int?,
): List<EpisodeBroadcastRuleUiState> {
    val targetRule = rules.firstOrNull { it.rowId == targetRowId }
        ?: return rules
    val episode = targetRule.episodeInput.toIntOrNull()
    val isDuplicate = episode != null && episode > 1 && rules.any { rule ->
        rule.rowId != targetRowId && rule.episodeInput.toIntOrNull() == episode
    }
    val targetError = when {
        targetRule.episodeInput.isBlank() -> R.string.bangumi_edit_error_rule_episode_required
        episode == null || episode <= 0 -> R.string.bangumi_edit_error_rule_episode_positive
        episode == 1 -> R.string.bangumi_edit_error_rule_first_episode
        totalEpisodes != null && episode > totalEpisodes -> R.string.bangumi_edit_error_rule_episode_exceeds_total
        isDuplicate -> R.string.bangumi_edit_error_rule_episode_duplicate
        else -> null
    }

    return rules.map { rule ->
        if (rule.rowId == targetRowId) {
            rule.copy(episodeError = targetError)
        } else {
            rule
        }
    }
}

internal fun validateAllRuleEpisodes(
    rules: List<EpisodeBroadcastRuleUiState>,
    totalEpisodes: Int?,
): List<EpisodeBroadcastRuleUiState> {
    val episodeCounts = rules
        .mapNotNull { it.episodeInput.toIntOrNull() }
        .filter { it > 1 }
        .groupingBy { it }
        .eachCount()

    return rules.map { rule ->
        val episode = rule.episodeInput.toIntOrNull()
        val error = when {
            rule.episodeInput.isBlank() -> R.string.bangumi_edit_error_rule_episode_required
            episode == null || episode <= 0 -> R.string.bangumi_edit_error_rule_episode_positive
            episode == 1 -> R.string.bangumi_edit_error_rule_first_episode
            totalEpisodes != null && episode > totalEpisodes -> R.string.bangumi_edit_error_rule_episode_exceeds_total
            episodeCounts[episode] != 1 -> R.string.bangumi_edit_error_rule_episode_duplicate
            else -> null
        }
        rule.copy(episodeError = error)
    }
}

internal fun validateRulesWhenSubmit(
    rules: List<EpisodeBroadcastRuleUiState>,
    totalEpisodes: Int?,
): List<EpisodeBroadcastRuleUiState> {
    return validateAllRuleEpisodes(
        rules = rules,
        totalEpisodes = totalEpisodes,
    ).map { rule ->
        rule.copy(
            ruleError = if (rule.ruleType == null) {
                R.string.bangumi_edit_error_rule_type_required
            } else {
                null
            },
            delayWeeksError = if (
                rule.ruleType == EpisodeBroadcastRuleType.DELAY
            ) {
                validateDelayWeeks(rule.delayWeeksInput)
            } else {
                null
            },
        )
    }
}

internal fun validateDelayWeeks(input: String): Int? {
    val weeks = input.toIntOrNull()
    return when {
        input.isBlank() -> R.string.bangumi_edit_error_delay_weeks_required
        weeks == null || weeks <= 0 -> R.string.bangumi_edit_error_delay_weeks_positive
        else -> null
    }
}

internal fun sortRuleRows(
    rules: List<EpisodeBroadcastRuleUiState>,
): List<EpisodeBroadcastRuleUiState> {
    return rules.sortedWith(
        compareBy<EpisodeBroadcastRuleUiState> { rule ->
            rule.episodeInput.toIntOrNull()?.takeIf { it > 0 } == null
        }.thenBy { rule ->
            rule.episodeInput.toIntOrNull()?.takeIf { it > 0 } ?: Int.MAX_VALUE
        }.thenBy(EpisodeBroadcastRuleUiState::rowId),
    )
}

internal fun EpisodeBroadcastRuleUiState.hasError(): Boolean {
    return episodeError != null || ruleError != null || delayWeeksError != null
}


