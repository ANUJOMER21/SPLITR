package com.omer.expensetracker.presentation.backup

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.BackupSummary
import com.omer.expensetracker.domain.model.ImportOutcome
import com.omer.expensetracker.domain.usecase.backup.ExportDataUseCase
import com.omer.expensetracker.domain.usecase.backup.ImportDataUseCase
import com.omer.expensetracker.domain.usecase.backup.PreviewImportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

private val Context.backupDataStore by preferencesDataStore(name = "backup_prefs")
private val LAST_BACKUP_KEY = longPreferencesKey("last_backup_epoch_millis")

data class BackupUiState(
    val lastBackupEpochMillis: Long? = null,
    val exportMessage: String? = null,
    val previewSummary: BackupSummary? = null,
    val importErrorMessage: String? = null,
    val importSuccess: Boolean = false
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val previewImportUseCase: PreviewImportUseCase,
    private val importDataUseCase: ImportDataUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val exportMessage = MutableStateFlow<String?>(null)
    private val previewSummary = MutableStateFlow<BackupSummary?>(null)
    private val importErrorMessage = MutableStateFlow<String?>(null)
    private val importSuccess = MutableStateFlow(false)
    private var pendingImportInput: (() -> InputStream?)? = null

    val uiState: StateFlow<BackupUiState> = combine(
        context.backupDataStore.data.map { it[LAST_BACKUP_KEY] },
        exportMessage, previewSummary, importErrorMessage, importSuccess
    ) { lastBackup, exportMsg, preview, importErr, success ->
        BackupUiState(lastBackup, exportMsg, preview, importErr, success)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BackupUiState())

    fun export(openOutputStream: () -> OutputStream?) {
        viewModelScope.launch {
            val out = openOutputStream()
            if (out == null) {
                exportMessage.value = "Couldn't open the destination file."
                return@launch
            }
            out.use { exportDataUseCase(it) }
            context.backupDataStore.edit { it[LAST_BACKUP_KEY] = System.currentTimeMillis() }
            exportMessage.value = "Backup saved."
        }
    }

    fun consumeExportMessage() {
        exportMessage.value = null
    }

    fun pickedImportFile(openInputStream: () -> InputStream?) {
        pendingImportInput = openInputStream
        viewModelScope.launch {
            val input = openInputStream()
            if (input == null) {
                importErrorMessage.value = "Couldn't open that file."
                return@launch
            }
            when (val outcome = input.use { previewImportUseCase(it) }) {
                is ImportOutcome.Preview -> previewSummary.value = outcome.summary
                is ImportOutcome.Invalid -> importErrorMessage.value = outcome.reason
                is ImportOutcome.Success -> Unit
            }
        }
    }

    fun confirmImport() {
        val opener = pendingImportInput ?: return
        viewModelScope.launch {
            val input = opener() ?: return@launch
            when (val outcome = input.use { importDataUseCase(it) }) {
                is ImportOutcome.Success -> {
                    previewSummary.value = null
                    importSuccess.value = true
                }
                is ImportOutcome.Invalid -> importErrorMessage.value = outcome.reason
                is ImportOutcome.Preview -> Unit
            }
        }
    }

    fun cancelImport() {
        previewSummary.value = null
        pendingImportInput = null
    }

    fun consumeImportError() {
        importErrorMessage.value = null
    }
}
