package com.copy9029.bangumimanagerreformed.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddBatchViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddSheetViewModel
import com.copy9029.bangumimanagerreformed.ui.bangumi.add.BangumiAddBatchScreen
import com.copy9029.bangumimanagerreformed.ui.bangumi.edit.BangumiEditScreen
import com.copy9029.bangumimanagerreformed.ui.bangumi.edit.BangumiEditViewModel
import com.copy9029.bangumimanagerreformed.ui.calendar.CalendarScreen
import com.copy9029.bangumimanagerreformed.ui.calendar.CalendarViewModel
import com.copy9029.bangumimanagerreformed.ui.index.IndexScreen
import com.copy9029.bangumimanagerreformed.ui.index.IndexViewModel
import com.copy9029.bangumimanagerreformed.ui.profile.ProfileScreen
import com.copy9029.bangumimanagerreformed.ui.profile.ProfileViewModel
import com.copy9029.bangumimanagerreformed.ui.profile.backup.BackupScreen
import com.copy9029.bangumimanagerreformed.ui.profile.backup.BackupViewModel
import com.copy9029.bangumimanagerreformed.ui.profile.color_settings.ColorSettingsScreen
import com.copy9029.bangumimanagerreformed.ui.profile.color_settings.ColorSettingsViewModel
import com.copy9029.bangumimanagerreformed.ui.profile.overview.OverviewScreen
import com.copy9029.bangumimanagerreformed.ui.profile.overview.OverviewViewModel
import com.copy9029.bangumimanagerreformed.ui.profile.statistics.StatisticsScreen
import com.copy9029.bangumimanagerreformed.ui.profile.statistics.StatisticsViewModel
import com.copy9029.bangumimanagerreformed.ui.calendar.settings.CalendarSettingsScreen
import com.copy9029.bangumimanagerreformed.ui.calendar.settings.CalendarSettingsViewModel

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
                            contentDescription = stringResource(destination.labelRes),
                        )
                    },
                    label = { Text(stringResource(destination.labelRes)) },
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
                val bangumiAddSheetViewModel: BangumiAddSheetViewModel = hiltViewModel()
                CalendarScreen(
                    calendarViewModel = calendarViewModel,
                    bangumiAddSheetViewModel = bangumiAddSheetViewModel,
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
                val bangumiAddSheetViewModel: BangumiAddSheetViewModel = hiltViewModel()
                IndexScreen(
                    indexViewModel = indexViewModel,
                    bangumiAddSheetViewModel = bangumiAddSheetViewModel,
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
                enterTransition = { secondaryPageEnterTransition() },
                exitTransition = { secondaryPageExitTransition() },
                popEnterTransition = { secondaryPageEnterTransition() },
                popExitTransition = { secondaryPageExitTransition() },
            ) {
                val backupViewModel: BackupViewModel = hiltViewModel()
                BackupScreen(
                    viewModel = backupViewModel,
                    onBack = navController::navigateUp,
                )
            }

            composable(
                route = Routes.COLOR_SETTINGS,
                enterTransition = { secondaryPageEnterTransition() },
                exitTransition = { secondaryPageExitTransition() },
                popEnterTransition = { secondaryPageEnterTransition() },
                popExitTransition = { secondaryPageExitTransition() },
            ) {
                val colorSettingsViewModel: ColorSettingsViewModel = hiltViewModel()
                ColorSettingsScreen(
                    viewModel = colorSettingsViewModel,
                    onBack = navController::navigateUp,
                )
            }

            composable(
                route = Routes.STATISTICS,
                enterTransition = { secondaryPageEnterTransition() },
                exitTransition = { secondaryPageExitTransition() },
                popEnterTransition = { secondaryPageEnterTransition() },
                popExitTransition = { secondaryPageExitTransition() },
            ) {
                val statisticsViewModel: StatisticsViewModel = hiltViewModel()
                StatisticsScreen(
                    viewModel = statisticsViewModel,
                    onBack = navController::navigateUp,
                )
            }

            composable(
                route = Routes.OVERVIEW,
                enterTransition = { secondaryPageEnterTransition() },
                exitTransition = { secondaryPageExitTransition() },
                popEnterTransition = { secondaryPageEnterTransition() },
                popExitTransition = { secondaryPageExitTransition() },
            ) {
                val overviewViewModel: OverviewViewModel = hiltViewModel()
                OverviewScreen(
                    viewModel = overviewViewModel,
                    onBack = navController::navigateUp,
                )
            }

            composable(
                route = Routes.BANGUMI_ADD_BATCH,
                enterTransition = { secondaryPageEnterTransition() },
                exitTransition = { secondaryPageExitTransition() },
                popEnterTransition = { secondaryPageEnterTransition() },
                popExitTransition = { secondaryPageExitTransition() },
            ) {
                val bangumiAddBatchViewModel: BangumiAddBatchViewModel = hiltViewModel()
                BangumiAddBatchScreen(
                    viewModel = bangumiAddBatchViewModel,
                    onBack = navController::navigateUp,
                )
            }

            composable(
                route = Routes.CALENDAR_SETTINGS,
                enterTransition = { secondaryPageEnterTransition() },
                exitTransition = { secondaryPageExitTransition() },
                popEnterTransition = { secondaryPageEnterTransition() },
                popExitTransition = { secondaryPageExitTransition() },
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
                enterTransition = { secondaryPageEnterTransition() },
                exitTransition = { secondaryPageExitTransition() },
                popEnterTransition = { secondaryPageEnterTransition() },
                popExitTransition = { secondaryPageExitTransition() },
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

private fun secondaryPageEnterTransition(): EnterTransition {
    return slideInHorizontally(
        animationSpec = tween(SECONDARY_PAGE_TRANSITION_DURATION_MILLIS),
        initialOffsetX = { fullWidth -> fullWidth },
    )
}

private fun secondaryPageExitTransition(): ExitTransition {
    return slideOutHorizontally(
        animationSpec = tween(SECONDARY_PAGE_TRANSITION_DURATION_MILLIS),
        targetOffsetX = { fullWidth -> fullWidth },
    )
}

private const val SECONDARY_PAGE_TRANSITION_DURATION_MILLIS = 300

private enum class BottomDestination(
    @param:StringRes val labelRes: Int,
    val icon: Int,
    val route: String,
) {
    CALENDAR(R.string.navigation_calendar, R.drawable.app_dest_calendar, "calendar"),
    INDEX(R.string.navigation_index, R.drawable.app_dest_index, "index"),
    PROFILE(R.string.navigation_profile, R.drawable.app_dest_profile, "profile"),
}

object Routes {
    const val BANGUMI_ID_ARGUMENT = "bangumiId"
    const val BANGUMI_EDIT = "bangumi_edit/{$BANGUMI_ID_ARGUMENT}"
    const val BANGUMI_ADD_BATCH = "bangumi_add_batch"
    const val CALENDAR_SETTINGS = "calendar_settings"
    const val BACKUP = "backup"
    const val COLOR_SETTINGS = "profile/color_settings"
    const val STATISTICS = "profile/statistics"
    const val OVERVIEW = "profile/overview"

    fun bangumiEdit(bangumiId: Int): String {
        return "bangumi_edit/$bangumiId"
    }
}
