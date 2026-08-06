package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.ui.navigation.Routes
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.themeColorByMonth
import com.copy9029.bangumimanagerreformed.util.latestAiredEpisode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class EpisodeBroadcastRuleType {
    DELAY,
    SAME_DAY_AS_PREVIOUS,
}

data class EpisodeBroadcastRuleUiState(
    val rowId: Long,    // Stable identifier within the editor lifecycle.
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
    val titleError: String? = null,
    val myScoreError: String? = null,
    val totalEpisodesError: String? = null,
    val latestWatchedEpisodeError: String? = null,
    val isSubmitting: Boolean = false,
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
    private val submitMutex = Mutex()
    private var nextRuleRowId = 1L

    init {
        viewModelScope.launch {
            val bangumi = repository.getBangumiById(bangumiId).first()
                ?: return@launch
            storedBangumi = bangumi
            val schedules = repository.getSchedulesByBangumiIdOnce(bangumiId)
            val today = LocalDate.now()
            val currentYear = today.year
            val defaultSeason = YearMonth.of(
                bangumi.seasonYear,
                bangumi.seasonMonth,
            )

            val parsedRules = schedules.toRuleList(
                firstBroadcastDate = bangumi.firstBroadcastDate,
            )
            nextRuleRowId = (parsedRules?.maxOfOrNull { it.rowId } ?: 0L) + 1L

            _uiState.value = BangumiEditUiState(
                bangumiId = bangumi.bangumiId,
                title = bangumi.title,
                seasonYear = defaultSeason.year,
                seasonMonth = defaultSeason.monthValue,
                seasonStartYear = minOf(defaultSeason.year, currentYear) - 5,
                seasonEndYear = maxOf(defaultSeason.year, currentYear) + 2,
                firstBroadcastDate = bangumi.firstBroadcastDate,
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
                episodeBroadcastRules = parsedRules,
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

    fun onFirstBroadcastDateChanged(date: LocalDate) {
        _uiState.update { state ->
            state?.takeIf { it.episodeBroadcastRules != null }  // 解析rules失败时禁用开播日期修改
                ?.copy(firstBroadcastDate = date)
                ?.recalculateLatestAiredEpisode()
                ?: state
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
            )?.recalculateLatestAiredEpisode()
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
        // 单调递增生成稳定标识，删除后也不复用旧 rowId。
        _uiState.update { state ->
            state ?: return@update null
            state.episodeBroadcastRules ?: return@update null

            state.copy(
                episodeBroadcastRules = state.episodeBroadcastRules +
                    EpisodeBroadcastRuleUiState(rowId = nextRuleRowId++),
            )
        }
    }

    fun onDeleteEpisodeBroadcastRule(rowId: Long) {
        // 删除时不校验其他行，并保留其他行的稳定标识。
        _uiState.update { state ->
            state ?: return@update null
            state.episodeBroadcastRules ?: return@update null

            state.copy(
                episodeBroadcastRules = state.episodeBroadcastRules.filterNot {
                    it.rowId == rowId
                },
            ).recalculateLatestAiredEpisode()
        }
    }

    fun onEpisodeBroadcastRuleEpisodeChanged(rowId: Long, value: String) {
        // 只校验正在修改的行，随后将有效集数升序排列；空行不会因其他行变化显示错误。
        _uiState.update { state ->
            state ?: return@update null
            state.episodeBroadcastRules ?: return@update null

            val updatedRules = state.episodeBroadcastRules.map { rule ->
                if (rule.rowId == rowId) {
                    rule.copy(episodeInput = value)
                } else {
                    rule
                }
            }

            state.copy(
                episodeBroadcastRules = sortRuleRows(
                    validateRuleEpisodes(
                        rules = updatedRules,
                        targetRowId = rowId,
                        totalEpisodes = state.totalEpisodesInput
                            .toIntOrNull()
                            ?.takeIf { it > 0 },
                    ),
                ),
            ).recalculateLatestAiredEpisode()
        }
    }

    fun onEpisodeBroadcastRuleTypeChanged(
        rowId: Long,
        ruleType: EpisodeBroadcastRuleType,
    ) {
        // 切换规则时清除类型错误；进入“停更”且周数为空时填入可编辑的默认值 1。
        _uiState.update { state ->
            state ?: return@update null
            state.episodeBroadcastRules ?: return@update null

            state.copy(
                episodeBroadcastRules = state.episodeBroadcastRules.map { rule ->
                    if (rule.rowId != rowId) {
                        rule
                    } else {
                        val delayWeeksInput = if (
                            ruleType == EpisodeBroadcastRuleType.DELAY &&
                                rule.delayWeeksInput.isBlank()
                        ) {
                            "1"
                        } else {
                            rule.delayWeeksInput
                        }

                        rule.copy(
                            ruleType = ruleType,
                            delayWeeksInput = delayWeeksInput,
                            ruleError = null,
                            delayWeeksError = if (ruleType == EpisodeBroadcastRuleType.DELAY) {
                                validateDelayWeeks(delayWeeksInput)
                            } else {
                                null
                            },
                        )
                    }
                },
            ).recalculateLatestAiredEpisode()
        }
    }

    fun onEpisodeBroadcastRuleDelayWeeksChanged(rowId: Long, value: String) {
        // 保留用户输入并立即校验，只有大于 0 且未溢出的整数才是有效停更周数。
        _uiState.update { state ->
            state ?: return@update null
            state.episodeBroadcastRules ?: return@update null

            state.copy(
                episodeBroadcastRules = state.episodeBroadcastRules.map { rule ->
                    if (rule.rowId == rowId) {
                        rule.copy(
                            delayWeeksInput = value,
                            delayWeeksError = validateDelayWeeks(value),
                        )
                    } else {
                        rule
                    }
                },
            ).recalculateLatestAiredEpisode()
        }
    }

    suspend fun onSubmitClick(): String {
        if (!submitMutex.tryLock()) return "修改正在提交，请稍候"

        _uiState.update { it?.copy(isSubmitting = true) }
        return try {
            submitChanges()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            "修改失败：保存时发生错误，请稍后重试"
        } finally {
            _uiState.update { it?.copy(isSubmitting = false) }
            submitMutex.unlock()
        }
    }

    private suspend fun submitChanges(): String {
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
        // 提交前对所有规则做完整校验，包括空集数、未选择类型和停更周数。
        val validatedRules = state.episodeBroadcastRules?.let {
            validateRulesWhenSubmit(
                state.episodeBroadcastRules,
                totalEpisodes = totalEpisodes,
            )
        }

        _uiState.update {
            it?.copy(
                titleError = titleError,
                myScoreError = myScoreError,
                totalEpisodesError = totalEpisodesError,
                latestWatchedEpisodeError = latestWatchedEpisodeError,
                episodeBroadcastRules = validatedRules,
            )
        }

        if (
            titleError != null ||
            myScoreError != null ||
            totalEpisodesError != null ||
            latestWatchedEpisodeError != null ||
            validatedRules?.any(EpisodeBroadcastRuleUiState::hasError) == true
            // 最早通过Schedules解析Rules失败时，validatedRules为null，最终不更新Schedules，也不报错
            // TODO：若今后允许锚点的直接编辑，此处应当添加校验
        ) {
            return "修改失败：请检查输入内容"
        }

        if (
            validatedRules == null &&
            state.firstBroadcastDate != bangumi.firstBroadcastDate
        ) {
            return "修改失败：日期锚点解析失败，无法修改开播日期"
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
            firstBroadcastDate = state.firstBroadcastDate,
            totalEpisodes = totalEpisodes,
            latestWatchedEpisode = requireNotNull(
                state.latestWatchedEpisodeInput.toIntOrNull()
            ),
            isActive = state.isActive,
        )
        val updatedSchedules = validatedRules?.toScheduleList(
            bangumiId = bangumiId,
            firstBroadcastDate = state.firstBroadcastDate,
        )
        storedBangumi = repository.updateBangumiAndSchedules(
            newBangumi = updatedBangumi,
            oldBangumi = bangumi,
            schedules = updatedSchedules,
        )

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

    private fun validateRuleEpisodes(
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
            targetRule.episodeInput.isBlank() -> "请输入集数"
            episode == null || episode <= 0 -> "集数必须是大于 0 的整数"
            episode == 1 -> "第 1 集没有上一集，无法设置播出规则"
            totalEpisodes != null && episode > totalEpisodes -> "集数不能大于总集数"
            isDuplicate -> "集数不能重复"
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

    private fun validateAllRuleEpisodes(
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
                rule.episodeInput.isBlank() -> "请输入集数"
                episode == null || episode <= 0 -> "集数必须是大于 0 的整数"
                episode == 1 -> "第 1 集没有上一集，无法设置播出规则"
                totalEpisodes != null && episode > totalEpisodes -> "集数不能大于总集数"
                episodeCounts[episode] != 1 -> "集数不能重复"
                else -> null
            }
            rule.copy(episodeError = error)
        }
    }

    private fun validateRulesWhenSubmit(
        rules: List<EpisodeBroadcastRuleUiState>,
        totalEpisodes: Int?,
    ): List<EpisodeBroadcastRuleUiState> {
        return validateAllRuleEpisodes(
            rules = rules,
            totalEpisodes = totalEpisodes,
        ).map { rule ->
            rule.copy(
                ruleError = if (rule.ruleType == null) {
                    "请选择播出规则"
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

    private fun validateDelayWeeks(input: String): String? {
        val weeks = input.toIntOrNull()
        return when {
            input.isBlank() -> "请输入停更周数"
            weeks == null || weeks <= 0 -> "停更周数必须是大于 0 的整数"
            else -> null
        }
    }

    private fun sortRuleRows(
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

    private fun BangumiEditUiState.recalculateLatestAiredEpisode(): BangumiEditUiState {
        val bangumi = storedBangumi ?: return this
        val rules = episodeBroadcastRules ?: return this
        val totalEpisodes = when {
            totalEpisodesInput.isBlank() -> null
            else -> totalEpisodesInput.toIntOrNull()
                ?.takeIf { it > 0 }
                ?: return this
        }
        val validatedRules = validateRulesWhenSubmit(
            rules = rules,
            totalEpisodes = totalEpisodes,
        )
        if (validatedRules.any(EpisodeBroadcastRuleUiState::hasError)) return this

        val schedules = try {
            validatedRules.toScheduleList(
                bangumiId = bangumiId,
                firstBroadcastDate = firstBroadcastDate,
            )
        } catch (_: RuntimeException) {
            return this
        }

        return copy(
            latestAiredEpisode = bangumi.copy(
                firstBroadcastDate = firstBroadcastDate,
                totalEpisodes = totalEpisodes,
            ).latestAiredEpisode(
                schedules = schedules,
                today = LocalDate.now(),
            ),
        )
    }
}

private fun EpisodeBroadcastRuleUiState.hasError(): Boolean {
    return episodeError != null || ruleError != null || delayWeeksError != null
}

private fun Int?.toScoreInput(): String {
    return when {
        this == null -> ""
        else -> (this / 10.0).toString()
    }
}


/**
 * 根据当前（数据库中）的Schedules推算RuleList，若推算失败则返回null
 */
private fun List<BangumiSchedule>.toRuleList(
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
private fun List<EpisodeBroadcastRuleUiState>.toScheduleList(
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
