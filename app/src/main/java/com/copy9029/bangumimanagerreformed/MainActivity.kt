package com.copy9029.bangumimanagerreformed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import com.copy9029.bangumimanagerreformed.ui.index.IndexScreen

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

@PreviewScreenSizes
@Composable
fun BangumiManagerReformedApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var currentDestination = navBackStackEntry?.destination

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { dest ->
                item(
                    icon = {
                        Icon(
                            painterResource(id = dest.icon),
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
                startDestination = AppDestinations.INDEX.route, // 初始页面
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppDestinations.CALENDAR.route) {
//                    CalendarScreen()
                    IndexScreen()
                }
                composable(AppDestinations.INDEX.route) {
                    IndexScreen()
                }
                composable(AppDestinations.PROFILE.route) {
//                    ProfileScreen()
                    IndexScreen()
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val route: String,
) {
    CALENDAR("Calendar", R.drawable.app_dest_calendar, "calendar"),
    INDEX("Index", R.drawable.app_dest_index, "index"),
    PROFILE("Profile", R.drawable.app_dest_profile, "profile"),
}
