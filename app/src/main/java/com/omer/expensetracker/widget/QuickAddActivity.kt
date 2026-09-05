package com.omer.expensetracker.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.glance.action.ActionParameters
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.presentation.components.AmountInputField
import com.omer.expensetracker.presentation.components.CategoryPickerGrid
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.ui.theme.AccentCoral
import com.omer.expensetracker.ui.theme.AccentTeal
import com.omer.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Launched straight from the home-screen "Quick add" widget: a bottom sheet over whatever
 * the user was looking at (usually the launcher), not the full app. Saves through the same
 * use cases the in-app Add screen uses, then finishes — no app UI is ever shown.
 */
@AndroidEntryPoint
class QuickAddActivity : ComponentActivity() {

    companion object {
        val EXTRA_TYPE = ActionParameters.Key<String>("entry_type")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialType = runCatching {
            EntryType.valueOf(intent.getStringExtra(EXTRA_TYPE.name) ?: "EXPENSE")
        }.getOrDefault(EntryType.EXPENSE)

        setContent {
            ExpenseTrackerTheme {
                QuickAddSheet(initialType = initialType, onFinished = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun QuickAddSheet(
    initialType: EntryType,
    onFinished: () -> Unit,
    viewModel: QuickAddViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState: SheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) { viewModel.setType(initialType) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onFinished() }

    ModalBottomSheet(
        onDismissRequest = onFinished,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                if (state.type == EntryType.EXPENSE) "Add Expense" else "Add Income",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AmountInputField(
                value = TextFieldValue(state.amountText, selection = androidx.compose.ui.text.TextRange(state.amountText.length)),
                onValueChange = { viewModel.onAmountChange(it.text) },
                errorMessage = state.errorMessage?.takeIf { it.contains("mount") }
            )

            DatePickerField(
                date = state.date,
                onDateChange = viewModel::onDateChange,
                modifier = Modifier.padding(top = 16.dp)
            )

            if (state.type == EntryType.EXPENSE) {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                )
                CategoryPickerGrid(
                    categories = state.categories,
                    selectedCategoryId = state.categoryId,
                    onSelect = { viewModel.onCategorySelect(it.id) }
                )
                val categoryError = state.errorMessage
                if (categoryError != null && categoryError.contains("ategory")) {
                    Text(
                        categoryError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            GradientButton(
                text = if (state.type == EntryType.EXPENSE) "Add Expense" else "Add Income",
                onClick = viewModel::save,
                gradient = if (state.type == EntryType.EXPENSE) listOf(AccentCoral, AccentCoral) else listOf(AccentTeal, AccentTeal),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp)
            )
        }
    }
}
