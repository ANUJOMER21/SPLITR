package com.omer.expensetracker.presentation.reminder

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.presentation.components.AmountInputField
import com.omer.expensetracker.presentation.components.CategoryPickerGrid
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.pressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBillReminderScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditBillReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) { if (state.isSaved) onDone() }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditing) "Edit reminder" else "New reminder") },
                navigationIcon = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.label,
                onValueChange = viewModel::onLabelChange,
                label = { Text("Label") },
                isError = state.errorMessage != null,
                supportingText = { state.errorMessage?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            AmountInputField(
                value = TextFieldValue(state.amountText, selection = TextRange(state.amountText.length)),
                onValueChange = { viewModel.onAmountChange(it.text) },
                errorMessage = null,
                modifier = Modifier.padding(top = 16.dp)
            )

            DatePickerField(
                date = state.dueDate,
                onDateChange = viewModel::onDueDateChange,
                modifier = Modifier.padding(top = 16.dp)
            )

            OutlinedTextField(
                value = state.leadDaysText,
                onValueChange = viewModel::onLeadDaysChange,
                label = { Text("Notify how many days before") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            Text("Category (optional)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            CategoryPickerGrid(
                categories = state.categories,
                selectedCategoryId = state.categoryId,
                onSelect = { viewModel.onCategorySelect(it.id) }
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Repeats", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Switch(checked = state.repeats, onCheckedChange = viewModel::onRepeatsChange)
            }
            if (state.repeats) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    listOf(RecurrenceUnit.WEEK, RecurrenceUnit.MONTH).forEachIndexed { index, u ->
                        SegmentedButton(
                            selected = state.repeatUnit == u,
                            onClick = { viewModel.onRepeatUnitChange(u) },
                            shape = SegmentedButtonDefaults.itemShape(index, 2),
                            colors = com.omer.expensetracker.presentation.components.themedSegmentedColors()
                        ) {
                            Text(if (u == RecurrenceUnit.WEEK) "Weekly" else "Monthly")
                        }
                    }
                }
            }

            com.omer.expensetracker.presentation.components.GradientButton(
                text = "Save",
                onClick = viewModel::save,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}
