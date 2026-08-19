package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.navigation.Routes
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.BangumiSchedule
import com.copy9029.bangumimanagerreformed.data.GlobalSettings
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
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


@HiltViewModel
class BangumiEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BangumiRepository,
    private val settingsRepository: SettingsRepository,
): ViewModel() {
    private val bangumiId = checkNotNull(
        savedStateHandle.get<Int>(Routes.BANGUMI_ID_ARGUMENT)
    )

    private val _uiState = MutableStateFlow<BangumiEditUiState?>(null)
    val uiState: StateFlow<BangumiEditUiState?> = _uiState.asStateFlow()

    private var storedBangumi: Bangumi? = null
    private var globalSettings = GlobalSettings()
    private val submitMutex = Mutex()
    private var nextRuleRowId = 1L

    init {
        viewModelScope.launch {
            settingsRepository.globalSettings.collect { settings ->
                globalSettings = settings
                _uiState.update { state ->
                    state?.copy(
                        themeColorLong = settings.colorForSeasonMonth(state.seasonMonth),
                    )
                }
            }
        }
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
                themeColorLong = globalSettings.colorForSeasonMonth(bangumi.seasonMonth),
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
                themeColorLong = globalSettings.colorForSeasonMonth(month),
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

    suspend fun onSubmitClick(): BangumiEditSubmitResult {
        if (!submitMutex.tryLock()) {
            return BangumiEditSubmitResult(R.string.bangumi_edit_submit_in_progress)
        }

        _uiState.update { it?.copy(isSubmitting = true) }
        return try {
            submitChanges()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            BangumiEditSubmitResult(R.string.bangumi_edit_submit_save_failure)
        } finally {
            _uiState.update { it?.copy(isSubmitting = false) }
            submitMutex.unlock()
        }
    }

    private suspend fun submitChanges(): BangumiEditSubmitResult {
        val state = _uiState.value
            ?: return BangumiEditSubmitResult(R.string.bangumi_edit_not_loaded)
        val bangumi = storedBangumi
            ?: return BangumiEditSubmitResult(R.string.bangumi_edit_not_loaded)

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
            return BangumiEditSubmitResult(R.string.bangumi_edit_submit_invalid)
        }

        if (
            validatedRules == null &&
            state.firstBroadcastDate != bangumi.firstBroadcastDate
        ) {
            return BangumiEditSubmitResult(R.string.bangumi_edit_anchor_parse_failure)
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

        return BangumiEditSubmitResult(R.string.bangumi_edit_submit_success)
    }

    // ==================== 输入校验 ====================

    private fun validateTitle(input: String): Int? {
        return if (input.isBlank()) R.string.bangumi_error_title_required else null
    }

    private fun validateMyScore(input: String): Int? {
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

    private fun validateTotalEpisodes(input: String): Int? {
        if (input.isBlank()) return null

        val totalEpisodes = input.toIntOrNull()
        return if (totalEpisodes != null && totalEpisodes > 0) {
            null
        } else {
            R.string.bangumi_edit_error_total_episodes_positive
        }
    }

    private fun validateLatestWatchedEpisode(
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

    private fun validateDelayWeeks(input: String): Int? {
        val weeks = input.toIntOrNull()
        return when {
            input.isBlank() -> R.string.bangumi_edit_error_delay_weeks_required
            weeks == null || weeks <= 0 -> R.string.bangumi_edit_error_delay_weeks_positive
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
