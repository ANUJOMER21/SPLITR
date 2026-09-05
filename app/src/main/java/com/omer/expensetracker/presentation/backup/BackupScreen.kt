package com.omer.expensetracker.presentation.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.components.pressScale
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.export { context.contentResolver.openOutputStream(uri) }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.pickedImportFile { context.contentResolver.openInputStream(uri) }
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Backup & restore") },
                navigationIcon = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Export", style = MaterialTheme.typography.titleMedium)
            Text(
                "Everything — categories, entries, budgets, recurring rules, reminders, and goals — into one file you save wherever you like.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            com.omer.expensetracker.presentation.components.GradientButton(
                text = "Export data",
                onClick = { exportLauncher.launch("expense_tracker_backup.json") }
            )
            state.lastBackupEpochMillis?.let { millis ->
                Text(
                    "Last backup: ${DateFormat.getDateTimeInstance().format(Date(millis))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            state.exportMessage?.let { message ->
                Text(message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }

            Text("Import", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 32.dp))
            Text(
                "Pick a previously exported file. You'll see what's inside before anything is overwritten.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            com.omer.expensetracker.presentation.components.GlassButton(
                text = "Choose backup file",
                onClick = { importLauncher.launch(arrayOf("application/json")) }
            )
            state.importErrorMessage?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
            if (state.importSuccess) {
                Text("Import complete.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    state.previewSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = viewModel::cancelImport,
            containerColor = com.omer.expensetracker.ui.theme.BackgroundBase,
            title = { Text("Import this backup?") },
            text = {
                Text(
                    "${summary.categoryCount} categories, ${summary.entryCount} entries, " +
                        "${summary.budgetCount} budgets, ${summary.recurringRuleCount} recurring rules, " +
                        "${summary.reminderCount} reminders, ${summary.goalCount} goals, " +
                        "${summary.contributionCount} contributions.\n\nThis replaces everything currently on this device."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmImport) { Text("Replace and import") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelImport) { Text("Cancel") }
            }
        )
    }
}
