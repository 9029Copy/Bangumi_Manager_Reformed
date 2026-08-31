package com.copy9029.bangumimanagerreformed.ui.profile

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.AppThemeMode
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onColorSettingsClick: () -> Unit,
    onOverviewClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreenContent(
        uiState = uiState,
        onThemeModeClick = viewModel::onThemeModeClick,
        onThemeModeDialogDismiss = viewModel::onThemeModeDialogDismiss,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onColorSettingsClick = onColorSettingsClick,
        onOverviewClick = onOverviewClick,
        onStatisticsClick = onStatisticsClick,
        onBackupClick = onBackupClick,
        modifier = modifier,
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onThemeModeClick: () -> Unit,
    onThemeModeDialogDismiss: () -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onColorSettingsClick: () -> Unit,
    onOverviewClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
        ) {
            item {
                UserProfileSection()
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            item {
                ProfileSettingItem(
                    title = stringResource(R.string.profile_appearance_mode),
                    value = stringResource(uiState.appThemeMode.displayTextRes),
                    onClick = onThemeModeClick,
                )
            }
            item {
                ProfileSettingItem(
                    title = stringResource(R.string.profile_color_settings_title),
                    value = "",
                    onClick = onColorSettingsClick,
                    previewContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ProfileColorSwatch(Color(uiState.default01ColorLong))
                            ProfileColorSwatch(Color(uiState.default04ColorLong))
                            ProfileColorSwatch(Color(uiState.default07ColorLong))
                            ProfileColorSwatch(Color(uiState.default10ColorLong))
                        }
                    },
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            item {
                ProfileSettingItem(
                    title = stringResource(R.string.profile_overview_title),
                    value = "",
                    onClick = onOverviewClick,
                )
            }
            item {
                ProfileSettingItem(
                    title = stringResource(R.string.profile_statistics_title),
                    value = "",
                    onClick = onStatisticsClick,
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            item {
                ProfileSettingItem(
                    title = stringResource(R.string.profile_backup),
                    value = "",
                    onClick = onBackupClick,
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            item {
                val context = LocalContext.current
                val versionName = remember(context) {
                    context.appVersionName()
                }
                Text(
                    text = stringResource(
                        R.string.profile_version,
                        versionName ?: stringResource(R.string.profile_version_unknown),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    if (uiState.isThemeModeDialogVisible) {
        ThemeModeDialog(
            selectedMode = uiState.appThemeMode,
            onModeSelected = onThemeModeSelected,
            onDismissRequest = onThemeModeDialogDismiss,
        )
    }
}

@Composable
private fun ThemeModeDialog(
    selectedMode: AppThemeMode,
    onModeSelected: (AppThemeMode) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.profile_appearance_mode)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RadioButton(
                            selected = mode == selectedMode,
                            onClick = null,
                        )
                        Text(
                            text = stringResource(mode.displayTextRes),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun UserProfileSection(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountBox,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ProfileSettingItem(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewContent: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                previewContent?.invoke()
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        modifier = modifier
            .padding(horizontal = 10.dp)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun ProfileColorSwatch(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(20.dp),
        shape = MaterialTheme.shapes.extraSmall,
        color = color,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = {},
    )
}

@get:StringRes
private val AppThemeMode.displayTextRes: Int
    get() = when (this) {
        AppThemeMode.FOLLOW_SYSTEM -> R.string.profile_theme_follow_system
        AppThemeMode.LIGHT -> R.string.profile_theme_light
        AppThemeMode.DARK -> R.string.profile_theme_dark
    }

@Suppress("DEPRECATION")
private fun Context.appVersionName(): String? {
    return runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName
    }.getOrNull()
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        ProfileScreenContent(
            uiState = ProfileUiState(
                appThemeMode = AppThemeMode.FOLLOW_SYSTEM,
            ),
            onThemeModeClick = {},
            onThemeModeDialogDismiss = {},
            onThemeModeSelected = {},
            onColorSettingsClick = {},
            onOverviewClick = {},
            onStatisticsClick = {},
            onBackupClick = {},
        )
    }
}
