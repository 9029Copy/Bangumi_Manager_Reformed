package com.copy9029.bangumimanagerreformed.ui.bangumi.add

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.BangumiAddInfo
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.GlobalSettings
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.util.calcNearestSeason
import com.copy9029.bangumimanagerreformed.util.parseMultipleFormattedText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddBatchUiState(
    val seasonYear: Int,
    val seasonMonth: Int,
    val formattedText: String = "",
    val startYear: Int,
    val endYear: Int,
    val items: List<BatchAddItemUiState> = emptyList(),
    val pendingDeleteItemId: Long? = null,
    val isSubmitting: Boolean = false,
    val themeColorLong: Long = 0xFFFFFFFFL,
) {
    val pendingDeleteItem: BatchAddItemUiState?
        get() = items.firstOrNull { it.id == pendingDeleteItemId }
}

data class BatchAddItemUiState(
    val id: Long,
    val title: String,
    val firstBroadcastDate: LocalDate,
    val titleError: Int? = null,
)

data class BatchParseResult(
    val successCount: Int,
    val totalCount: Int,
    val errorIndexes: List<Int>,
)

sealed interface BatchSubmitResult {
    data class Success(val itemCount: Int) : BatchSubmitResult

    data class Failure(
        @param:StringRes val messageRes: Int,
    ) : BatchSubmitResult
}

@HiltViewModel
class BangumiAddBatchViewModel @Inject constructor(
    private val repository: BangumiRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private var globalSettings = GlobalSettings()
    private var nextItemId = 1L

    private val defaultSeason = calcNearestSeason(LocalDate.now())

    private val _uiState = MutableStateFlow(
        AddBatchUiState(
            seasonYear = defaultSeason.year,
            seasonMonth = defaultSeason.monthValue,
            startYear = defaultSeason.year - 5,
            endYear = defaultSeason.year + 2,
            themeColorLong = globalSettings.colorForSeasonMonth(defaultSeason.monthValue),
        )
    )
    val uiState: StateFlow<AddBatchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.globalSettings.collect { settings ->
                globalSettings = settings
                _uiState.update { state ->
                    state.copy(
                        themeColorLong = settings.colorForSeasonMonth(state.seasonMonth),
                    )
                }
            }
        }
    }

    fun onSeasonChanged(year: Int, month: Int) {
        _uiState.update {
            it.copy(
                seasonYear = year,
                seasonMonth = month,
                themeColorLong = globalSettings.colorForSeasonMonth(month),
            )
        }
    }

    fun onFormattedTextChanged(value: String) {
        _uiState.update {
            it.copy(formattedText = value)
        }
    }

    fun onParseClick(): BatchParseResult {
        val parsedList = parseMultipleFormattedText(_uiState.value.formattedText)

        val items = parsedList.mapNotNull { parsed ->
            parsed?.let {
                BatchAddItemUiState(
                    id = nextItemId++,
                    title = it.title,
                    firstBroadcastDate = it.firstBroadcastDate,
                )
            }
        }

        val errorIndexes = parsedList
            .mapIndexedNotNull { index, parsed ->
                if (parsed == null) index + 1 else null
            }

        _uiState.update {
            it.copy(items = items)
        }

        return BatchParseResult(
            successCount = items.size,
            totalCount = parsedList.size,
            errorIndexes = errorIndexes,
        )
    }

    fun onTitleChanged(itemId: Long, title: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(
                            title = title,
                            titleError = null,
                        )
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun onFirstBroadcastDateChanged(itemId: Long, date: LocalDate) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(firstBroadcastDate = date)
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun onRequestDeleteItem(itemId: Long) {
        _uiState.update {
            it.copy(pendingDeleteItemId = itemId)
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update {
            it.copy(pendingDeleteItemId = null)
        }
    }

    fun onConfirmDeleteItem() {
        val itemId = _uiState.value.pendingDeleteItemId ?: return

        _uiState.update { state ->
            state.copy(
                items = state.items.filterNot { it.id == itemId },
                pendingDeleteItemId = null,
            )
        }
    }

    suspend fun onSubmitAllClick(): BatchSubmitResult {
        val state = _uiState.value
        if (state.isSubmitting) {
            return BatchSubmitResult.Failure(R.string.bangumi_add_submit_in_progress)
        }

        val validatedItems = state.items.map { item ->
            if (item.title.isBlank()) {
                item.copy(titleError = R.string.bangumi_error_title_required)
            } else {
                item
            }
        }

        _uiState.update {
            it.copy(items = validatedItems)
        }

        if (validatedItems.isEmpty()) {
            return BatchSubmitResult.Failure(R.string.bangumi_add_submit_empty)
        }
        if (validatedItems.any { it.titleError != null }) {
            return BatchSubmitResult.Failure(R.string.bangumi_add_submit_invalid)
        }

        val infos = validatedItems.map { item ->
            BangumiAddInfo(
                seasonYear = state.seasonYear,
                seasonMonth = state.seasonMonth,
                title = item.title.trim(),
                firstBroadcastDate = item.firstBroadcastDate,
            )
        }

        _uiState.update {
            it.copy(isSubmitting = true)
        }

        return try {
            repository.addNewBangumisBatch(infos)

            _uiState.update {
                it.copy(
                    formattedText = "",
                    items = emptyList(),
                    pendingDeleteItemId = null,
                    isSubmitting = false,
                )
            }
            BatchSubmitResult.Success(infos.size)
        } catch (exception: CancellationException) {
            _uiState.update {
                it.copy(isSubmitting = false)
            }
            throw exception
        } catch (_: Exception) {
            _uiState.update {
                it.copy(isSubmitting = false)
            }
            BatchSubmitResult.Failure(R.string.bangumi_add_submit_database_failure)
        }
    }
}
