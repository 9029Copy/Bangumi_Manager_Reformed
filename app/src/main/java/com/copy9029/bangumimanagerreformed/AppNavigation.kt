package com.copy9029.bangumimanagerreformed

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.AddBatchViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.AddSheetViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddBatchScreen
import com.copy9029.bangumimanagerreformed.ui.bangumi.edit.BangumiEditScreen
import com.copy9029.bangumimanagerreformed.ui.bangumi.edit.BangumiEditViewModel
import com.copy9029.bangumimanagerreformed.ui.backup.BackupScreen
import com.copy9029.bangumimanagerreformed.ui.backup.BackupViewModel
import com.copy9029.bangumimanagerreformed.ui.calendar.CalendarScreen
import com.copy9029.bangumimanagerreformed.ui.calendar.CalendarViewModel
import com.copy9029.bangumimanagerreformed.ui.index.IndexScreen
import com.copy9029.bangumimanagerreformed.ui.index.IndexViewModel
import com.copy9029.bangumimanagerreformed.ui.profile.ProfileScreen
import com.copy9029.bangumimanagerreformed.ui.profile.ProfileViewModel
import com.copy9029.bangumimanagerreformed.ui.settings.CalendarSettingsScreen
import com.copy9029.bangumimanagerreformed.ui.settings.CalendarSettingsViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelDestination = BottomDestination.entries.any { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }
    val defaultNavigationSuiteType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            currentWindowAdaptiveInfo()
        )

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        layoutType = if (isTopLevelDestination) {
            defaultNavigationSuiteType
        } else {
            NavigationSuiteType.None
        },
        navigationSuiteItems = {
            BottomDestination.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            painter = painterResource(destination.icon),
                            contentDescription = destination.label,
                        )
                    },
                    label = { Text(destination.label) },
                    selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = BottomDestination.CALENDAR.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(
                route = BottomDestination.CALENDAR.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                val calendarViewModel: CalendarViewModel = hiltViewModel()
                val addSheetViewModel: AddSheetViewModel = hiltViewModel()
                CalendarScreen(
                    calendarViewModel = calendarViewModel,
                    addSheetViewModel = addSheetViewModel,
                    onEditClick = { bangumiId ->
                        navController.navigate(Routes.bangumiEdit(bangumiId))
                    },
                    onBatchClick = {
                        navController.navigate(Routes.BANGUMI_ADD_BATCH)
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.CALENDAR_SETTINGS)
                    },
                )
            }

            composable(
                route = BottomDestination.INDEX.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                val indexViewModel: IndexViewModel = hiltViewModel()
                val addSheetViewModel: AddSheetViewModel = hiltViewModel()
                IndexScreen(
                    indexViewModel = indexViewModel,
                    addSheetViewModel = addSheetViewModel,
                    onEditClick = { bangumiId ->
                        navController.navigate(Routes.bangumiEdit(bangumiId))
                    },
                    onBatchClick = {
                        navController.navigate(Routes.BANGUMI_ADD_BATCH)
                    },
                )
            }

            composable(
                route = BottomDestination.PROFILE.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBackupClick = {
                        navController.navigate(Routes.BACKUP)
                    },
                )
            }

            composable(
                route = Routes.BACKUP,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                val backupViewModel: BackupViewModel = hiltViewModel()
                BackupScreen(
                    viewModel = backupViewModel,
                    onBack = navController::navigateUp,
                )
            }

            composable(
                route = Routes.BANGUMI_ADD_BATCH,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                val addBatchViewModel: AddBatchViewModel = hiltViewModel()
                BangumiAddBatchScreen(
                    viewModel = addBatchViewModel,
                    onBack = navController::navigateUp,
                )
            }

            composable(
                route = Routes.CALENDAR_SETTINGS,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                val settingsViewModel: CalendarSettingsViewModel = hiltViewModel()
                CalendarSettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = navController::navigateUp,
                )
            }

            composable(
                route = Routes.BANGUMI_EDIT,
                arguments = listOf(
                    navArgument(Routes.BANGUMI_ID_ARGUMENT) {
                        type = NavType.IntType
                    }
                ),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                val editViewModel: BangumiEditViewModel = hiltViewModel()
                BangumiEditScreen(
                    viewModel = editViewModel,
                    onBack = navController::navigateUp,
                )
            }
        }
    }
}

private enum class BottomDestination(
    val label: String,
    val icon: Int,
    val route: String,
) {
    CALENDAR("日历", R.drawable.app_dest_calendar, "calendar"),
    INDEX("列表", R.drawable.app_dest_index, "index"),
    PROFILE("个人", R.drawable.app_dest_profile, "profile"),
}

object Routes {
    const val BANGUMI_ID_ARGUMENT = "bangumiId"
    const val BANGUMI_EDIT = "bangumi_edit/{$BANGUMI_ID_ARGUMENT}"
    const val BANGUMI_ADD_BATCH = "bangumi_add_batch"
    const val CALENDAR_SETTINGS = "calendar_settings"
    const val BACKUP = "backup"

    fun bangumiEdit(bangumiId: Int): String {
        return "bangumi_edit/$bangumiId"
    }
}
