package com.copy9029.bangumimanagerreformed.ui.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.copy9029.bangumimanagerreformed.R
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

    LaunchedEffect(viewModel, context) {
        viewModel.messages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("数据备份与恢复") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
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
                headlineContent = { Text("导出备份") },
                supportingContent = { Text("将应用数据保存到备份文件") },
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
                headlineContent = { Text("导入备份") },
                supportingContent = { Text("从备份文件恢复应用数据") },
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
}

private const val BACKUP_MIME_TYPE = "application/json"
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
            uiState = BackupUiState(),
            onBack = {},
            onExportClick = {},
            onImportClick = {},
        )
    }
}
