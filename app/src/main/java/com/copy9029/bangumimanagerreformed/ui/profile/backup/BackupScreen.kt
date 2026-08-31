package com.copy9029.bangumimanagerreformed.ui.profile.backup

import android.content.res.Resources
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
import com.copy9029.bangumimanagerreformed.data.backup.BackupSection
import com.copy9029.bangumimanagerreformed.data.backup.BackupValidationIssue
import com.copy9029.bangumimanagerreformed.data.backup.BangumiValidationReason
import com.copy9029.bangumimanagerreformed.data.backup.CalendarSettingValidationReason
import com.copy9029.bangumimanagerreformed.data.backup.ScheduleValidationReason
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun BackupScreen(
    viewModel: BackupViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE),
        onResult = { uri ->
            uri?.let(viewModel::onExportDestinationSelected)
        },
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let(viewModel::onImportFileSelected)
        },
    )

    LaunchedEffect(viewModel, context, resources) {
        viewModel.messages.collect { message ->
            Toast.makeText(
                context,
                message.resolve(resources),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    BackupScreenContent(
        uiState = uiState,
        onBack = onBack,
        onExportClick = {
            exportLauncher.launch(createBackupFileName())
        },
        onImportClick = {
            importLauncher.launch(BACKUP_MIME_TYPES)
        },
        onImportConfirm = viewModel::onImportConfirm,
        onImportDismiss = viewModel::onImportDismiss,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupScreenContent(
    uiState: BackupUiState,
    onBack: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onImportConfirm: () -> Unit,
    onImportDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.backup_export)) },
                supportingContent = {
                    Text(stringResource(R.string.backup_export_description))
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.backup_upload_24),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(
                    enabled = !uiState.isExporting && !uiState.isImporting,
                    onClick = onExportClick,
                ),
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.backup_import)) },
                supportingContent = {
                    Text(stringResource(R.string.backup_import_description))
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.backup_download_24),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(
                    enabled = !uiState.isExporting && !uiState.isImporting,
                    onClick = onImportClick,
                ),
            )
        }
    }

    uiState.importConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = onImportDismiss,
            title = {
                Text(
                    text = stringResource(R.string.backup_import_confirmation_title),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(
                            R.string.backup_import_summary,
                            confirmation.bangumiCount,
                            confirmation.scheduleCount,
                            confirmation.settingCount,
                        )
                    )
                    Text(
                        text = stringResource(R.string.backup_import_overwrite_warning),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onImportConfirm,
                    enabled = !uiState.isImporting,
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onImportDismiss,
                    enabled = !uiState.isImporting,
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

private fun BackupMessage.resolve(resources: Resources): String {
    return when (this) {
        is BackupMessage.ExportSuccess -> resources.getString(
            if (exceedsImportSizeLimit) {
                R.string.backup_export_success_over_limit
            } else {
                R.string.backup_export_success
            },
            fileSizeText,
        )
        BackupMessage.ExportFailure -> resources.getString(R.string.backup_export_failure)
        is BackupMessage.ReadTooLarge -> resources.getString(
            R.string.backup_read_too_large,
            maxSizeMiB,
        )
        is BackupMessage.ReadInvalid -> resources.getString(
            R.string.backup_read_invalid,
            issue.resolve(resources),
        )
        BackupMessage.ReadFailure -> resources.getString(R.string.backup_read_failure)
        BackupMessage.ImportSuccess -> resources.getString(R.string.backup_import_success)
        BackupMessage.ImportFailure -> resources.getString(R.string.backup_import_failure)
    }
}

private fun BackupValidationIssue.resolve(resources: Resources): String {
    return when (this) {
        is BackupValidationIssue.MissingField -> resources.getString(
            R.string.backup_validation_missing_field,
            section.resolve(resources),
            fieldName,
        )
        is BackupValidationIssue.UnsupportedFormatVersion -> resources.getString(
            R.string.backup_validation_unsupported_version,
            version,
        )
        BackupValidationIssue.MalformedJson -> resources.getString(
            R.string.backup_validation_malformed_json,
        )
        is BackupValidationIssue.InvalidDateOrTime -> value?.let {
            resources.getString(
                R.string.backup_validation_invalid_date_time_value,
                it.take(MAX_ERROR_VALUE_LENGTH),
            )
        } ?: resources.getString(R.string.backup_validation_invalid_date_time)
        BackupValidationIssue.InvalidContent -> resources.getString(
            R.string.backup_validation_invalid_content,
        )
        BackupValidationIssue.InvalidExportedAt -> resources.getString(
            R.string.backup_validation_invalid_exported_at,
        )
        BackupValidationIssue.BlankAppVersion -> resources.getString(
            R.string.backup_validation_blank_app_version,
        )
        BackupValidationIssue.DuplicateBangumiId -> resources.getString(
            R.string.backup_validation_duplicate_bangumi_id,
        )
        is BackupValidationIssue.InvalidBangumi -> resources.getString(
            R.string.backup_validation_bangumi_detail,
            bangumiId,
            reason.resolve(resources),
        )
        is BackupValidationIssue.InvalidSchedule -> resources.getString(
            R.string.backup_validation_schedule_detail,
            bangumiId,
            episodeId,
            reason.resolve(resources),
        )
        is BackupValidationIssue.MissingFirstSchedule -> resources.getString(
            R.string.backup_validation_missing_first_schedule,
            bangumiId,
        )
        is BackupValidationIssue.FirstScheduleDateMismatch -> resources.getString(
            R.string.backup_validation_first_schedule_date_mismatch,
            bangumiId,
        )
        is BackupValidationIssue.InvalidCalendarSetting -> resources.getString(
            R.string.backup_validation_calendar_detail,
            reason.resolve(resources),
        )
        is BackupValidationIssue.InvalidColor -> resources.getString(
            R.string.backup_validation_invalid_color,
            month,
        )
        is BackupValidationIssue.UnknownThemeMode -> resources.getString(
            R.string.backup_validation_unknown_theme_mode,
            storedValue.take(MAX_ERROR_VALUE_LENGTH),
        )
        is BackupValidationIssue.ExpectedEndDateOutOfRange -> resources.getString(
            R.string.backup_validation_expected_end_date_out_of_range,
            bangumiId,
        )
    }
}

private fun BackupSection.resolve(resources: Resources): String = when (this) {
    BackupSection.Root -> resources.getString(R.string.backup_validation_section_root)
    is BackupSection.BangumiItem -> resources.getString(
        R.string.backup_validation_section_bangumi,
        index,
    )
    is BackupSection.ScheduleItem -> resources.getString(
        R.string.backup_validation_section_schedule,
        index,
    )
    BackupSection.CalendarSettings -> resources.getString(
        R.string.backup_validation_section_calendar_settings,
    )
    BackupSection.GlobalSettings -> resources.getString(
        R.string.backup_validation_section_global_settings,
    )
}

private fun BangumiValidationReason.resolve(resources: Resources): String = resources.getString(
    when (this) {
        BangumiValidationReason.ID_NON_POSITIVE -> R.string.backup_validation_bangumi_id_non_positive
        BangumiValidationReason.TITLE_BLANK -> R.string.backup_validation_bangumi_title_blank
        BangumiValidationReason.SEASON_YEAR_OUT_OF_RANGE -> R.string.backup_validation_bangumi_season_year
        BangumiValidationReason.SEASON_MONTH_INVALID -> R.string.backup_validation_bangumi_season_month
        BangumiValidationReason.SCORE_OUT_OF_RANGE -> R.string.backup_validation_bangumi_score
        BangumiValidationReason.TOTAL_EPISODES_NON_POSITIVE -> R.string.backup_validation_bangumi_total_episodes
        BangumiValidationReason.WATCHED_EPISODE_NEGATIVE -> R.string.backup_validation_bangumi_watched_negative
        BangumiValidationReason.WATCHED_EPISODE_EXCEEDS_TOTAL -> R.string.backup_validation_bangumi_watched_exceeds_total
        BangumiValidationReason.LAST_MODIFIED_NEGATIVE -> R.string.backup_validation_bangumi_last_modified
    },
)

private fun ScheduleValidationReason.resolve(resources: Resources): String = resources.getString(
    when (this) {
        ScheduleValidationReason.MISSING_BANGUMI_REFERENCE -> R.string.backup_validation_schedule_missing_bangumi
        ScheduleValidationReason.EPISODE_NON_POSITIVE -> R.string.backup_validation_schedule_episode_non_positive
        ScheduleValidationReason.DUPLICATE -> R.string.backup_validation_schedule_duplicate
        ScheduleValidationReason.EPISODE_EXCEEDS_TOTAL -> R.string.backup_validation_schedule_exceeds_total
    },
)

private fun CalendarSettingValidationReason.resolve(resources: Resources): String =
    resources.getString(
        when (this) {
            CalendarSettingValidationReason.INACTIVE_VISIBILITY_INVALID -> R.string.backup_validation_calendar_visibility
            CalendarSettingValidationReason.WEEKS_BEFORE_OUT_OF_RANGE -> R.string.backup_validation_calendar_weeks_before
            CalendarSettingValidationReason.WEEKS_AFTER_OUT_OF_RANGE -> R.string.backup_validation_calendar_weeks_after
            CalendarSettingValidationReason.WEEKS_PREFIX_OUT_OF_RANGE -> R.string.backup_validation_calendar_weeks_prefix
        },
    )

private const val BACKUP_MIME_TYPE = "application/json"
private const val MAX_ERROR_VALUE_LENGTH = 64
private val BACKUP_MIME_TYPES = arrayOf(
    BACKUP_MIME_TYPE,
    "application/octet-stream",
)
private val BACKUP_FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

private fun createBackupFileName(): String {
    val timestamp = LocalDateTime.now().format(BACKUP_FILE_TIME_FORMATTER)
    return "BangumiManager_$timestamp.bmbackup"
}

@Preview(showBackground = true)
@Composable
private fun BackupScreenPreview() {
    BangumiManagerReformedTheme(dynamicColor = false) {
        BackupScreenContent(
            uiState = BackupUiState(
                importConfirmation = BackupImportConfirmation(
                    1,2,3,
                ),
            ),
            onBack = {},
            onExportClick = {},
            onImportClick = {},
            onImportConfirm = {},
            onImportDismiss = {},
        )
    }
}
