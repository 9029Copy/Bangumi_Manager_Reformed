package com.copy9029.bangumimanagerreformed.ui.bangumi.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy9029.bangumimanagerreformed.data.BangumiAddInfo
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.data.themeColorByMonth
import com.copy9029.bangumimanagerreformed.util.calcNearestSeason
import com.copy9029.bangumimanagerreformed.util.parseMultipleFormattedText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
) {
    val themeColorLong: Long
        get() = themeColorByMonth[seasonMonth] ?: 0xFFFFFFFFL

    val pendingDeleteItem: BatchAddItemUiState?
        get() = items.firstOrNull { it.id == pendingDeleteItemId }
}

data class BatchAddItemUiState(
    val id: Long,
    val title: String,
    val firstBroadcastDate: LocalDate,
    val titleError: String? = null,
)

data class BatchParseResult(
    val successCount: Int,
    val totalCount: Int,
    val errorIndexes: List<Int>,
) {
    val message: String
        get() {
            val base = "成功解析${successCount}/${totalCount}个项目"
            return if (errorIndexes.isEmpty()) {
                base
            } else {
                "$base，错误项目编号为${errorIndexes.joinToString("/")}"
            }
        }
}

@HiltViewModel
class AddBatchViewModel @Inject constructor(
    private val repository: BangumiRepository,
) : ViewModel() {
    private var nextItemId = 1L

    private val defaultSeason = calcNearestSeason(LocalDate.now())

    private val _uiState = MutableStateFlow(
        AddBatchUiState(
            seasonYear = defaultSeason.year,
            seasonMonth = defaultSeason.monthValue,
            startYear = defaultSeason.year - 5,
            endYear = defaultSeason.year + 2,
        )
    )
    val uiState: StateFlow<AddBatchUiState> = _uiState.asStateFlow()

    fun onSeasonChanged(year: Int, month: Int) {
        _uiState.update {
            it.copy(
                seasonYear = year,
                seasonMonth = month,
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

    fun onSubmitAllClick(): String {
        val state = _uiState.value

        val validatedItems = state.items.map { item ->
            if (item.title.isBlank()) {
                item.copy(titleError = "标题不能为空")
            } else {
                item
            }
        }

        _uiState.update {
            it.copy(items = validatedItems)
        }

        if (validatedItems.isEmpty()) {
            return "F添加失败：待添加列表为空！"
        }
        if (validatedItems.any { it.titleError != null }) {
            return "F添加失败：请检查项目是否无误！"
        }

        val infos = validatedItems.map { item ->
            BangumiAddInfo(
                seasonYear = state.seasonYear,
                seasonMonth = state.seasonMonth,
                themeColorLong = state.themeColorLong,
                title = item.title.trim(),
                firstBroadcastDate = item.firstBroadcastDate,
            )
        }

        viewModelScope.launch {
            repository.addNewBangumisBatch(infos)

            _uiState.update {
                it.copy(
                    formattedText = "",
                    items = emptyList(),
                    pendingDeleteItemId = null,
                )
            }
        }

        return "T成功添加 ${infos.size} 个项目！"
    }
}
