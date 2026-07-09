package com.copy9029.bangumimanagerreformed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
//import com.copy9029.bangumimanager.data.BangumiDisplayModel
//import com.copy9029.bangumimanager.data.BangumiItem
//import com.copy9029.bangumimanager.data.BangumiProgress
//import com.copy9029.bangumimanager.data.BangumiRepository
//import com.copy9029.bangumimanager.data.WatchStatus
//import com.copy9029.bangumimanager.util.calculateBangumiDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth


class BangumiViewModel() {

}

//data class CalendarUiState(
//    val currentMonth: YearMonth = YearMonth.now(),
//    val bangumiList: List<BangumiItem> = emptyList(),
//    val allProgress: List<BangumiProgress> = emptyList(),
//    val isLoading: Boolean = false,
////    val userMessage: String? = null
//)
//
//class BangumiViewModel(private val repository: BangumiRepository) : ViewModel() {
//
//    companion object {
//        fun provideFactory(repository: BangumiRepository): ViewModelProvider.Factory =
//            viewModelFactory {
//                initializer {
//                    BangumiViewModel(repository)
//                }
//            }
//    }
//
//    // 内部私有状态，主要用来控制当前月份
//    private val _currentMonth = MutableStateFlow(YearMonth.now())
//
//    // 组合数据流：番剧列表 + 进度列表 + 当前月份
//    val uiState: StateFlow<PageCalendarUiState> = combine(
//        _currentMonth,
//        repository.getAllBangumis(), // 假设 Repository 提供 Flow<List<BangumiItem>>
//        repository.getAllProgress()  // 假设 Repository 提供 Flow<List<BangumiProgress>>
//    ) { month, bangumis, progress ->
//        PageCalendarUiState(
//            currentMonth = month,
//            bangumiList = bangumis,
//            allProgress = progress,
//            isLoading = false
//        )
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.Eagerly,
//        initialValue = PageCalendarUiState()
//    )
//
//    // --- 业务逻辑：月份切换 ---
//    fun onMonthChange(newMonth: YearMonth) {
//        _currentMonth.value = newMonth
//    }
//
//    // 添加新项目
//    fun addNewBangumi(name: String, startDate: LocalDate, season: YearMonth) {
//        viewModelScope.launch {
//            val newItem = BangumiItem(
//                id = 0, // Room 会自动处理自增 ID
//                name = name,
//                startDate = startDate,
//                dayOfWeek = startDate.dayOfWeek.value,
//                season = season,
//                endDate = null,
//                isActive = true,
//            )
//            repository.addBangumi(newItem)
//        }
//    }
//
//    // --- 业务逻辑：状态更新 ---
//    fun updateBangumiStatus(bangumiId: Int, date: LocalDate, status: WatchStatus) {
//        viewModelScope.launch {
//            repository.updateProgress(bangumiId, date, status)
//        }
//    }
//
//    // --- 核心计算函数：给 UI 调用 ---
//    // 这个函数将原始数据转化为 UI 渲染用的显示模型
//    fun getBangumisForDate(date: LocalDate): List<BangumiDisplayModel> {
//        val state = uiState.value
//
//        return state.bangumiList.mapNotNull { bangumi ->
//            calculateBangumiDisplay(bangumi, state.allProgress, date)
//        }
//
//    }
//
//    fun toggleBangumiStatus(model: BangumiDisplayModel, date: LocalDate) {
//        val nextStatus = when (model.status) {
//            WatchStatus.UNWATCHED -> WatchStatus.WATCHED   // 点击未看 -> 变已看
//            WatchStatus.WATCHED -> WatchStatus.SUSPEND     // 点击已看 -> 变停更
//            WatchStatus.SUSPEND -> WatchStatus.UNWATCHED // 点击停更 -> 回到未看
//        }
//
//        viewModelScope.launch {
//            repository.updateProgress(model.id, date, nextStatus)
//        }
//    }
//
//
//}