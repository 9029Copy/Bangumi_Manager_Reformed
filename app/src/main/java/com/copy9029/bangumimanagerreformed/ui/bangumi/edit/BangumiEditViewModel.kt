package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.Routes
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.themeColorByMonth
import com.copy9029.bangumimanagerreformed.util.latestAiredEpisode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

enum class EpisodeBroadcastRuleType {
    DELAY,
    SAME_DAY_AS_PREVIOUS,
}

data class EpisodeBroadcastRuleUiState(
    val rowId: Long,    // begin with 1
    val episodeInput: String = "",
    val ruleType: EpisodeBroadcastRuleType? = null,
    val delayWeeksInput: String = "1",
    val episodeError: String? = null,
    val ruleError: String? = null,
    val delayWeeksError: String? = null,
)

data class BangumiEditUiState(
    val bangumiId: Int,

    // 第一部分：基本信息
    val title: String = "",
    val seasonYear: Int,
    val seasonMonth: Int,
    val seasonStartYear: Int,
    val seasonEndYear: Int,
    val myScoreInput: String = "",
    val isActive: Boolean = true,

    // 第二部分：集数与观看进度
    val totalEpisodesInput: String = "",
    val latestWatchedEpisodeInput: String = "0",
    val latestAiredEpisode: Int = 0,

    // 第三部分：分集播出规则
    val episodeBroadcastRules: List<EpisodeBroadcastRuleUiState> = emptyList(),

    // 表单错误
    val titleError: String? = null,
    val myScoreError: String? = null,
    val totalEpisodesError: String? = null,
    val latestWatchedEpisodeError: String? = null,
) {
    val themeColorLong: Long
        get() = themeColorByMonth[seasonMonth] ?: 0xFFFFFFFFL

}


@HiltViewModel
class BangumiEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BangumiRepository,
): ViewModel() {
    companion object {
        const val SUBMIT_SUCCESS = "success"
    }

    private val bangumiId = checkNotNull(
        savedStateHandle.get<Int>(Routes.BANGUMI_ID_ARGUMENT)
    )

    private val _uiState = MutableStateFlow<BangumiEditUiState?>(null)
    val uiState: StateFlow<BangumiEditUiState?> = _uiState.asStateFlow()

    private var storedBangumi: Bangumi? = null

    init {
        viewModelScope.launch {
            val bangumi = repository.getBangumiById(bangumiId).first()
                ?: return@launch
            storedBangumi = bangumi
            val schedules = repository.getSchedulesByBangumiId(bangumiId)
            val today = LocalDate.now()
            val currentYear = today.year
            val defaultSeason = YearMonth.of(
                bangumi.seasonYear,
                bangumi.seasonMonth,
            )

            _uiState.value = BangumiEditUiState(
                bangumiId = bangumi.bangumiId,
                title = bangumi.title,
                seasonYear = defaultSeason.year,
                seasonMonth = defaultSeason.monthValue,
                seasonStartYear = minOf(defaultSeason.year, currentYear) - 5,
                seasonEndYear = maxOf(defaultSeason.year, currentYear) + 2,
                myScoreInput = bangumi.myScore.toScoreInput(),
                isActive = bangumi.isActive,
                totalEpisodesInput = bangumi.totalEpisodes?.toString().orEmpty(),
                latestWatchedEpisodeInput = bangumi.latestWatchedEpisode.toString(),
                latestAiredEpisode = bangumi.latestAiredEpisode(
                    schedules = schedules,
                    today = today,
                ),
                titleError = null,
                myScoreError = null,
                totalEpisodesError = null,
                latestWatchedEpisodeError = null,
//                episodeBroadcastRules = // TODO: 根据Schedule推算Rules,
            )
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update {
            it?.copy(
                title = value,
                titleError = null,
            )
        }
    }

    fun onSeasonChanged(year: Int, month: Int) {
        _uiState.update {
            it?.copy(
                seasonYear = year,
                seasonMonth = month,
            )
        }
    }

    fun onMyScoreChanged(value: String) {
        _uiState.update {
            it?.copy(
                myScoreInput = value,
                myScoreError = null,
            )
        }
    }

    fun onHiddenChanged(hidden: Boolean) {
        _uiState.update {
            it?.copy(isActive = !hidden)
        }
    }

    fun onTotalEpisodesChanged(value: String) {
        _uiState.update {
            it?.copy(
                totalEpisodesInput = value,
                totalEpisodesError = null,
            )
        }
    }

    fun onLatestWatchedEpisodeChanged(value: String) {
        _uiState.update {
            it?.copy(
                latestWatchedEpisodeInput = value,
                latestWatchedEpisodeError = null,
            )
        }
    }

    fun onSetWatchedToMinimum() {
        _uiState.update {
            it?.copy(
                latestWatchedEpisodeInput = "0",
                latestWatchedEpisodeError = null,
            )
        }
    }

    fun onWatchedEpisodeMinusOne() {
        val current = _uiState.value
            ?.latestWatchedEpisodeInput
            ?.toIntOrNull()
            ?: return
        _uiState.update {
            it?.copy(
                latestWatchedEpisodeInput = (current - 1)
                    .coerceAtLeast(0)
                    .toString(),
                latestWatchedEpisodeError = null,
            )
        }
    }

    fun onWatchedEpisodePlusOne() {
        _uiState.update { state ->
            state ?: return@update null

            val current = state.latestWatchedEpisodeInput.toIntOrNull()
                ?: return@update state
            val totalEpisodes = state.totalEpisodesInput
                .toIntOrNull()
                ?.takeIf { it > 0 }
            val incremented = if (current == Int.MAX_VALUE) current else current + 1
            val next = totalEpisodes?.let { incremented.coerceAtMost(it) }
                ?: incremented

            state.copy(
                latestWatchedEpisodeInput = next.toString(),
                latestWatchedEpisodeError = null,
            )
        }
    }

    fun onSetWatchedToMaximum() {
        _uiState.update { state ->
            state?.let {
                val maximum = it.totalEpisodesInput.toIntOrNull()
                    ?: it.latestAiredEpisode
                it.copy(
                    latestWatchedEpisodeInput = maximum
                        .coerceAtLeast(0)
                        .toString(),
                    latestWatchedEpisodeError = null,
                )
            }
        }
    }

    fun onAddEpisodeBroadcastRule() {
        // TODO: 新增一条集数与规则均为空的播出规则。
    }

    fun onDeleteEpisodeBroadcastRule(rowId: Long) {
        // TODO: 删除指定播出规则。
    }

    fun onEpisodeBroadcastRuleEpisodeChanged(rowId: Long, value: String) {
        // TODO: 校验并更新指定规则的正整数集数。
    }

    fun onEpisodeBroadcastRuleTypeChanged(
        rowId: Long,
        ruleType: EpisodeBroadcastRuleType,
    ) {
        // TODO: 更新规则类型，并处理停更周数输入状态。
    }

    fun onEpisodeBroadcastRuleDelayWeeksChanged(rowId: Long, value: String) {
        // TODO: 校验并更新大于 0 的停更周数。
    }

    suspend fun onSubmitClick(): String {
        val state = _uiState.value
            ?: return "项目尚未加载完成"
        val bangumi = storedBangumi
            ?: return "项目尚未加载完成"

        val titleError = validateTitle(state.title)
        val myScoreError = validateMyScore(state.myScoreInput)
        val totalEpisodesError = validateTotalEpisodes(state.totalEpisodesInput)
        val totalEpisodes = state.totalEpisodesInput.toIntOrNull()
        val latestWatchedEpisodeError = validateLatestWatchedEpisode(
            input = state.latestWatchedEpisodeInput,
            totalEpisodes = totalEpisodes,
        )

        _uiState.update {
            it?.copy(
                titleError = titleError,
                myScoreError = myScoreError,
                totalEpisodesError = totalEpisodesError,
                latestWatchedEpisodeError = latestWatchedEpisodeError,
            )
        }

        if (
            titleError != null ||
            myScoreError != null ||
            totalEpisodesError != null ||
            latestWatchedEpisodeError != null
        ) {
            return "修改失败：请检查输入内容"
        }

        val updatedBangumi = bangumi.copy(
            title = state.title.trim(),
            seasonYear = state.seasonYear,
            seasonMonth = state.seasonMonth,
            myScore = state.myScoreInput
                .takeIf(String::isNotBlank)
                ?.toBigDecimal()
                ?.movePointRight(1)
                ?.intValueExact(),
            themeColorLong = state.themeColorLong,
            totalEpisodes = totalEpisodes,
            latestWatchedEpisode = requireNotNull(
                state.latestWatchedEpisodeInput.toIntOrNull()
            ),
            isActive = state.isActive,
        )

        repository.updateBangumi(
            newBangumi = updatedBangumi,
            oldBangumi = bangumi,
        )
        storedBangumi = updatedBangumi

        return SUBMIT_SUCCESS
    }

    // ==================== 输入校验 ====================

    private fun validateTitle(input: String): String? {
        return if (input.isBlank()) "标题不能为空" else null
    }

    private fun validateMyScore(input: String): String? {
        if (input.isBlank()) return null
        if (!input.matches(Regex("""\d+(?:\.\d+)?"""))) {
            return "评分只能包含数字和一个小数点"
        }

        val scoreTimesTen = try {
            input.toBigDecimal()
                .movePointRight(1)
                .intValueExact()
        } catch (_: ArithmeticException) {
            return "评分最多保留一位小数"
        } catch (_: NumberFormatException) {
            return "评分格式不正确"
        }

        return if (scoreTimesTen in 0..100) {
            null
        } else {
            "评分必须在 0.0 到 10.0 之间"
        }
    }

    private fun validateTotalEpisodes(input: String): String? {
        if (input.isBlank()) return null

        val totalEpisodes = input.toIntOrNull()
        return if (totalEpisodes != null && totalEpisodes > 0) {
            null
        } else {
            "总集数必须是大于 0 的整数"
        }
    }

    private fun validateLatestWatchedEpisode(
        input: String,
        totalEpisodes: Int?,
    ): String? {
        val latestWatchedEpisode = input.toIntOrNull()
            ?: return "已观看集数必须是大于或等于 0 的整数"

        if (latestWatchedEpisode < 0) {
            return "已观看集数必须大于或等于 0"
        }
        if (totalEpisodes != null && latestWatchedEpisode > totalEpisodes) {
            return "已观看集数不能大于总集数"
        }

        return null
    }
}

private fun Int?.toScoreInput(): String {
    return when {
        this == null -> ""
        else -> (this / 10.0).toString()
    }
}
