package com.copy9029.bangumimanagerreformed.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.AddSheetViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheet
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.util.generateBangumiColorScheme

@Composable
fun PageCalendarScreen(
    calendarViewModel: CalendarViewModel,
    addSheetViewModel: AddSheetViewModel,
    onBatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    var isAddSheetVisible by rememberSaveable {
        mutableStateOf(false)
    }

    CalendarScreenContent(
        uiState = uiState,
        onAddClick = {
            isAddSheetVisible = true
        },
        modifier = modifier,
    )

    if (isAddSheetVisible) {
        BangumiAddSheet(
            viewModel = addSheetViewModel,
            defaultFirstBroadcastDate = LocalDate.now(),
            onDismissRequest = {
                isAddSheetVisible = false
            },
            onBatchClick = {
                isAddSheetVisible = false
                onBatchClick()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScreenContent(
    uiState: CalendarUiState,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = uiState.initialWeekIndex,
    )
    val displayedMonth by remember(uiState.firstWeekStart, listState) {
        derivedStateOf {
            YearMonth.from(
                uiState.firstWeekStart
                    .plusWeeks(listState.firstVisibleItemIndex.toLong())
                    .plusDays(3),
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("${displayedMonth.year} 年 ${displayedMonth.monthValue} 月")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        floatingActionButton = {
            SmallFloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加项目",
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            DaysOfWeekHeader()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                items(
                    count = uiState.weekCount,
                    key = { weekIndex ->
                        uiState.firstWeekStart
                            .plusWeeks(weekIndex.toLong())
                            .toEpochDay()
                    },
                ) { weekIndex ->
                    CalendarWeekRow(
                        weekStart = uiState.firstWeekStart
                            .plusWeeks(weekIndex.toLong()),
                        bangumisByDate = uiState.bangumisByDate,
                    )
                }
            }
        }
    }
}

@Composable
private fun DaysOfWeekHeader(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp),
    ) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun CalendarWeekRow(
    weekStart: LocalDate,
    bangumisByDate: Map<LocalDate, List<CalendarBangumiItemUiState>>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 6.dp),
    ) {
        repeat(7) { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            CalendarDateCell(
                date = date,
                isToday = date == LocalDate.now(),
                bangumis = bangumisByDate[date].orEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun CalendarDateCell(
    date: LocalDate,
    isToday: Boolean,
    bangumis: List<CalendarBangumiItemUiState>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(1.dp),
        shape = RoundedCornerShape(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (isToday) {
                    Text(
                        text = "今",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }

            }

            bangumis.forEach { item ->
                CalendarBangumiTag(item)
            }
        }
    }
}

@Composable
private fun CalendarBangumiTag(
    item: CalendarBangumiItemUiState,
    modifier: Modifier = Modifier,
) {
    val colorScheme = generateBangumiColorScheme(item.themeColorLong)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.cardContainer,
        shape = RoundedCornerShape(3.dp),
    ) {
        Text(
            text = item.title,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
            color = colorScheme.primaryContent,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}




@Preview(showBackground = true)
@Composable
private fun PreviewCalendarScreenContent() {
    val today = LocalDate.now()
    BangumiManagerReformedTheme(dynamicColor = false) {
        CalendarScreenContent(
            uiState = CalendarUiState(
                firstWeekStart = today.minusDays(
                    (today.dayOfWeek.value - 1).toLong()
                ),
                weekCount = 3,
                initialWeekIndex = 0,
                bangumisByDate = mapOf(
                    today to listOf(
                        CalendarBangumiItemUiState(
                            bangumiId = 1,
                            episodeId = 3,
                            title = "示例动画",
                            themeColorLong = 0xFF80FFFFL,
                        ),
                    ),
                ),
            ),
            onAddClick = {},
        )
    }
}

// ==================== 旧版 HorizontalPager 实现（保留供参考） ====================

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
