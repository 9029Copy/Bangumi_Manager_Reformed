package com.copy9029.bangumimanagerreformed.ui.bangumi.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.navigation.Routes
import com.copy9029.bangumimanagerreformed.data.Bangumi
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
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
import javax.inject.Inject

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

private fun Int?.toScoreInput(): String {
    return when {
        this == null -> ""
        else -> (this / 10.0).toString()
    }
}
