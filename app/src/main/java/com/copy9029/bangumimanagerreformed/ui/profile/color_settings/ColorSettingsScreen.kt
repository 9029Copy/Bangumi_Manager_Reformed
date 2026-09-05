package com.copy9029.bangumimanagerreformed.ui.profile.color_settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.SettingsRepository
import com.copy9029.bangumimanagerreformed.ui.components.AppColorPickerDialog
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme

@Composable
fun ColorSettingsScreen(
    viewModel: ColorSettingsViewModel,
    onBack: () -> Unit,
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

    ColorSettingsScreenContent(
        uiState = uiState,
        onBack = onBack,
        onDefault01ColorClick = viewModel::onDefault01ColorClick,
        onDefault04ColorClick = viewModel::onDefault04ColorClick,
        onDefault07ColorClick = viewModel::onDefault07ColorClick,
        onDefault10ColorClick = viewModel::onDefault10ColorClick,
        onDefaultColorDialogDismiss = viewModel::onDefaultColorDialogDismiss,
        onDefaultColorSelected = viewModel::onDefaultColorSelected,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSettingsScreenContent(
    uiState: ColorSettingsUiState,
    onBack: () -> Unit,
    onDefault01ColorClick: () -> Unit,
    onDefault04ColorClick: () -> Unit,
    onDefault07ColorClick: () -> Unit,
    onDefault10ColorClick: () -> Unit,
    onDefaultColorDialogDismiss: () -> Unit,
    onDefaultColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_color_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
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
                ColorSettingsPreviewSection(
                    default01ColorLong = uiState.default01ColorLong,
                    default04ColorLong = uiState.default04ColorLong,
                    default07ColorLong = uiState.default07ColorLong,
                    default10ColorLong = uiState.default10ColorLong,
                    initiallyExpanded = false,
                )
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun ColorSettingsScreenPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        ColorSettingsScreenContent(
            uiState = ColorSettingsUiState(
                default01ColorLong = SettingsRepository.DEFAULT_01_COLOR_LONG,
                default04ColorLong = SettingsRepository.DEFAULT_04_COLOR_LONG,
                default07ColorLong = SettingsRepository.DEFAULT_07_COLOR_LONG,
                default10ColorLong = SettingsRepository.DEFAULT_10_COLOR_LONG,
            ),
            onBack = {},
            onDefault01ColorClick = {},
            onDefault04ColorClick = {},
            onDefault07ColorClick = {},
            onDefault10ColorClick = {},
            onDefaultColorDialogDismiss = {},
            onDefaultColorSelected = {},
        )
    }
}
