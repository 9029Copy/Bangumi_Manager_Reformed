package com.copy9029.bangumimanagerreformed.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.AddSheetViewModel

@Composable
fun PageCalendarScreen(
    calendarViewModel: CalendarViewModel,
    addSheetViewModel: AddSheetViewModel,
    onEditClick: (Int) -> Unit,
    onBatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

}

//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PageCalendarScreen(
//    viewModel: BangumiViewModel,
//    modifier: Modifier = Modifier,
//    onAddTask: () -> Unit,
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//
//    val initialPage = 500
//    val pagerState = rememberPagerState(initialPage = initialPage) { 1000 }
//
//    LaunchedEffect(pagerState.currentPage) {
//        val monthOffset = (pagerState.currentPage - initialPage).toLong()
//        val targetMonth = YearMonth.now().plusMonths(monthOffset)
//        viewModel.onMonthChange(targetMonth)
//    }
//
//    Scaffold(
//        modifier = modifier.fillMaxSize(),
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(text = "月份页视图", fontSize = 20.sp)
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.primary,
//                ),
////                navigationIcon = {
////                    IconButton(
////                        onClick = {
////                            navController.popBackStack()
////                        }
////                    ) {
////                        Icon(Icons.Filled.ArrowBack, contentDescription = "Arrow back")
////                    }
////                }
//            )
//        },
//        floatingActionButton = {
//            SmallFloatingActionButton(onClick = onAddTask) {
//                Icon(Icons.Filled.Add, "Add item")
//            }
//        },
//    ) { padding ->
//        Column(
//            modifier = Modifier.padding(padding)) {
//            // 1. 悬停年月显示
//            MonthHeader(uiState.currentMonth)
//
//            // 2. 悬停星期显示
//            DaysOfWeekHeader()
//
//            // 3. 日历主体（横向翻页+纵向滚动）
//            HorizontalPager(
//                state = pagerState,
//                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
//                verticalAlignment = Alignment.Top, // 确保顶部对齐
//                contentPadding = PaddingValues(horizontal = 10.dp),
//                pageSpacing = 4.dp
//            ) { page ->
//
//                val monthOffset = (page - initialPage).toLong()
//                val displayMonth = remember(page) {
//                    YearMonth.now().plusMonths(monthOffset)
//                }
//
//                val calendarDates = remember(displayMonth) {
//                    getCalendarDates(displayMonth)
//                }
//
//                LazyVerticalGrid(
//                    columns = GridCells.Fixed(7),
//                    modifier = Modifier.fillMaxSize(),
//                    // 如果需要单元格之间有分割线，可以加 contentPadding
//                    contentPadding = PaddingValues(1.dp)
//                ) {
//                    items(calendarDates.size) { index ->
//                        val date = calendarDates[index]
//                        // 根据日期从 ViewModel 获取当天的剧集
//                        val displayModels = remember(date, uiState) {
//                            viewModel.getBangumisForDate(date)
//                        }
//
//                        DateCell(
//                            date = date,
//                            isCurrentMonth = date.month == displayMonth.month ,
//                            displayModels = displayModels
//                        )
//                    }
//                }
//
//            }
//
//        }
//    }
//}
//
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun MonthHeader(yearMonth: YearMonth) {
//    Surface(
//        color = MaterialTheme.colorScheme.secondaryContainer,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Text(
//            text = "${yearMonth.year}年 ${yearMonth.monthValue}月",
//            modifier = Modifier.padding(8.dp),
//            style = MaterialTheme.typography.titleMedium,
//            textAlign = TextAlign.Center
//        )
//    }
//}
//
//@Composable
//fun DaysOfWeekHeader() {
//    val days = listOf("一", "二", "三", "四", "五", "六", "日")
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(MaterialTheme.colorScheme.surfaceVariant)
//    ) {
//        days.forEach { day ->
//            Text(
//                text = day,
//                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.labelSmall
//            )
//        }
//    }
//}
//
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun DateCell(
//    date: LocalDate,
//    isCurrentMonth: Boolean,
//    displayModels: List<BangumiDisplayModel>
//) {
//    // 单元格设计：略竖长，高度由内容决定
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(1.dp)
//            .defaultMinSize(minHeight = 200.dp), // 设置最小高度保证视觉效果
//        shape = RoundedCornerShape(2.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        ),
//        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
//    ) {
//        Column(
//            modifier = Modifier.fillMaxWidth().padding(6.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // 数字在中间上方
//            Column(modifier = Modifier.fillMaxWidth().aspectRatio(0.8F), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(
//                    text = date.dayOfMonth.toString(),
//                    textAlign = TextAlign.Center,
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = if (isCurrentMonth) Color.Unspecified else Color.Gray,
//                )
//                Spacer(modifier = Modifier.size(5.dp))
//                Text(
//                    text = "农历",
//                    textAlign = TextAlign.Center,
//                    fontSize = 18.sp,
//                    color = if (isCurrentMonth) Color.Unspecified else Color.Gray,
//                )
//            }
//
//            Spacer(modifier = Modifier.height(5.dp))
//
//            // 中间偏下方显示当天更新内容
//            displayModels.forEach { displayModel ->
//                DramaTag(displayModel)
//            }
//        }
//    }
//}
//
//@Composable
//fun DramaTag(displayModel: BangumiDisplayModel) {
//    Surface(
//        color = MaterialTheme.colorScheme.primaryContainer,
//        shape = RoundedCornerShape(2.dp),
//        modifier = Modifier.padding(vertical = 1.dp).fillMaxWidth(),
//    ) {
//        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
//            Text(
//                text = displayModel.name,
//                fontSize = 13.sp,
//                maxLines = 1,
//                overflow = TextOverflow.Clip,
//                modifier = Modifier.padding(2.dp),
//            )
//        }
//
//    }
//}

//@Preview
//@Composable
//fun thisPreview() {
//    DateCell(
//        LocalDate.now(),
//        true,
//        emptyList()
//    )
//}