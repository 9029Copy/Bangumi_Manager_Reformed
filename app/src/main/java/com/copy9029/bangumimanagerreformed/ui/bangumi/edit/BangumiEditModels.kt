package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import androidx.annotation.StringRes
import java.time.LocalDate

enum class EpisodeBroadcastRuleType {
    DELAY,
    SAME_DAY_AS_PREVIOUS,
}

data class EpisodeBroadcastRuleUiState(
    val rowId: Long,    // Stable identifier within the editor lifecycle.
    val episodeInput: String = "",
    val ruleType: EpisodeBroadcastRuleType? = null,
    val delayWeeksInput: String = "1",
    val episodeError: Int? = null,
    val ruleError: Int? = null,
    val delayWeeksError: Int? = null,
)

data class BangumiEditUiState(
    val bangumiId: Int,

    // 第一部分：基本信息
    val title: String = "",
    val seasonYear: Int,
    val seasonMonth: Int,
    val seasonStartYear: Int,
    val seasonEndYear: Int,
    val firstBroadcastDate: LocalDate,
    val myScoreInput: String = "",
    val isActive: Boolean = true,

    // 第二部分：集数与观看进度
    val totalEpisodesInput: String = "",
    val latestWatchedEpisodeInput: String = "0",
    val latestAiredEpisode: Int = 0,

    // 第三部分：分集播出规则
    val episodeBroadcastRules: List<EpisodeBroadcastRuleUiState>? = emptyList(),

    // 表单错误
    val titleError: Int? = null,
    val myScoreError: Int? = null,
    val totalEpisodesError: Int? = null,
    val latestWatchedEpisodeError: Int? = null,

    val isSubmitting: Boolean = false,
    val themeColorLong: Long = 0xFFFFFFFFL,
)

data class BangumiEditSubmitResult(
    @param:StringRes val messageRes: Int,
)
