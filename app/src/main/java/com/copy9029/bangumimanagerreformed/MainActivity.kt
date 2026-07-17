package com.copy9029.bangumimanagerreformed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.copy9029.bangumimanagerreformed.data.BangumiRepository
import com.copy9029.bangumimanagerreformed.ui.bangumi.BangumiEditScreen
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.ui.index.IndexScreen
import com.copy9029.bangumimanagerreformed.ui.index.IndexViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BangumiManagerReformedTheme {
                BangumiManagerReformedApp()
            }
        }
    }
}

@Composable
fun BangumiManagerReformedApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            BottomDestination.entries.forEach { dest ->
                item(
                    icon = {
                        Icon(
                            painter = painterResource(id = dest.icon),
                            contentDescription = dest.label
                        )
                    },
                    label = { Text(dest.label) },
                    selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                    onClick = {
                        navController.navigate(dest.route) {
                            // 弹出到导航图的起始目的地，避免栈内堆积页面
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // 避免多次点击同一个目的地时重复创建实例
                            launchSingleTop = true
                            // 重新选择之前选中的目的地时，恢复状态
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomDestination.INDEX.route, // 初始页面
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomDestination.CALENDAR.route) {
//                    CalendarScreen()
                }
                composable(BottomDestination.INDEX.route) {
                    val indexViewModel: IndexViewModel = hiltViewModel()
                    IndexScreen(
                        viewModel = indexViewModel,
                        onEditClick = { bangumiId ->
                            navController.navigate(Routes.bangumiEdit(bangumiId))
                        },
                    )
                }
                composable(BottomDestination.PROFILE.route) {
//                    ProfileScreen()
                }

                composable(
                    route = Routes.BANGUMI_EDIT,
                    arguments = listOf(
                        navArgument("bangumiId") { type = NavType.IntType }
                    ),
                ) { backStackEntry ->
                    val bangumiId = backStackEntry.arguments?.getInt("bangumiId")
                        ?: return@composable

                    BangumiEditScreen(
                        bangumiId = bangumiId,
                        onBack = navController::navigateUp,
                    )
                }
            }
        }
    }
}

enum class BottomDestination(
    val label: String,
    val icon: Int,
    val route: String,
) {
    CALENDAR("Calendar", R.drawable.app_dest_calendar, "calendar"),
    INDEX("Index", R.drawable.app_dest_index, "index"),
    PROFILE("Profile", R.drawable.app_dest_profile, "profile"),
}

object Routes {
    const val BANGUMI_EDIT = "bangumi_edit/{bangumiId}"
    fun bangumiEdit(bangumiId: Int): String {
        return "bangumi_edit/${bangumiId}"
    }
}