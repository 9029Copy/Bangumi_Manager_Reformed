package com.copy9029.bangumimanagerreformed.ui.profile

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.AppThemeMode
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.ui.components.AppColorPickerDialog
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current

    LaunchedEffect(viewModel, context, resources) {
        viewModel.toastMessages.collect { message ->
            Toast.makeText(
                context,
                resources.getString(
                    R.string.profile_color_updated,
                    message.month,
                    message.argbHex,
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onThemeModeClick = viewModel::onThemeModeClick,
        onThemeModeDialogDismiss = viewModel::onThemeModeDialogDismiss,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onDefault01ColorClick = viewModel::onDefault01ColorClick,
        onDefault04ColorClick = viewModel::onDefault04ColorClick,
        onDefault07ColorClick = viewModel::onDefault07ColorClick,
        onDefault10ColorClick = viewModel::onDefault10ColorClick,
        onDefaultColorDialogDismiss = viewModel::onDefaultColorDialogDismiss,
        onDefaultColorSelected = viewModel::onDefaultColorSelected,
        onBackupClick = onBackupClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onThemeModeClick: () -> Unit,
    onThemeModeDialogDismiss: () -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onDefault01ColorClick: () -> Unit,
    onDefault04ColorClick: () -> Unit,
    onDefault07ColorClick: () -> Unit,
    onDefault10ColorClick: () -> Unit,
    onDefaultColorDialogDismiss: () -> Unit,
    onDefaultColorSelected: (Long) -> Unit,
    onBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
//        topBar = {
//            TopAppBar(
//                title = { Text("个人") },
//                actions = { // TODO
//                    IconButton(onClick = onTopActionClick) {
//                        Icon(
//                            imageVector = Icons.Filled.MoreVert,
//                            contentDescription = "更多",
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.primary,
//                ),
//            )
//        },
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
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            item {
                ColorSettingItem(
                    title = stringResource(R.string.profile_default_color, 1),
                    colorLong = uiState.default01ColorLong,
                    onClick = onDefault01ColorClick,
                )
            }
            item {
                ColorSettingItem(
                    title = stringResource(R.string.profile_default_color, 4),
                    colorLong = uiState.default04ColorLong,
                    onClick = onDefault04ColorClick,
                )
            }
            item {
                ColorSettingItem(
                    title = stringResource(R.string.profile_default_color, 7),
                    colorLong = uiState.default07ColorLong,
                    onClick = onDefault07ColorClick,
                )
            }
            item {
                ColorSettingItem(
                    title = stringResource(R.string.profile_default_color, 10),
                    colorLong = uiState.default10ColorLong,
                    onClick = onDefault10ColorClick,
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

    val colorPickerMonth = uiState.colorPickerMonth
    val colorPickerInitialColorLong = uiState.colorPickerInitialColorLong
    if (colorPickerMonth != null && colorPickerInitialColorLong != null) {
        AppColorPickerDialog(
            title = stringResource(R.string.profile_default_color, colorPickerMonth),
            initColor = Color(colorPickerInitialColorLong),
            onDismissRequest = onDefaultColorDialogDismiss,
            onPickedColor = { selectedColor ->
                onDefaultColorSelected(selectedColor.toArgb().toUInt().toLong())
            },
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
//                modifier = Modifier.,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
private fun UserProfileSection( // TODO：个人Profile
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
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                SettingItemArrow()
            }
        },
        modifier = modifier
            .padding(horizontal = 10.dp)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun ColorSettingItem(
    title: String,
    colorLong: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorSwatch(color = Color(colorLong))
                SettingItemArrow()
            }
        },
        modifier = modifier
            .padding(horizontal = 10.dp)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun SettingItemArrow() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ColorSwatch(
    color: Color,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Surface(
        modifier = modifier.size(28.dp),
        shape = shape,
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
                default01ColorLong = SettingsRepository.DEFAULT_01_COLOR_LONG,
                default04ColorLong = SettingsRepository.DEFAULT_04_COLOR_LONG,
                default07ColorLong = SettingsRepository.DEFAULT_07_COLOR_LONG,
                default10ColorLong = SettingsRepository.DEFAULT_10_COLOR_LONG,
                isThemeModeDialogVisible = false,
                colorPickerMonth = null,
                colorPickerInitialColorLong = null,
            ),
            onThemeModeClick = {},
            onThemeModeDialogDismiss = {},
            onThemeModeSelected = {},
            onDefault01ColorClick = {},
            onDefault04ColorClick = {},
            onDefault07ColorClick = {},
            onDefault10ColorClick = {},
            onDefaultColorDialogDismiss = {},
            onDefaultColorSelected = {},
            onBackupClick = {},
        )
    }
}
