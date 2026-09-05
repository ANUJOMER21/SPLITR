package com.omer.expensetracker.presentation.recurring

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.presentation.components.AmountInputField
import com.omer.expensetracker.presentation.components.CategoryPickerGrid
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.pressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringRuleScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditRecurringRuleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) { if (state.isSaved) onDone() }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditing) "Edit recurring rule" else "New recurring rule") },
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
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(EntryType.EXPENSE, EntryType.INCOME).forEachIndexed { index, t ->
                    SegmentedButton(
                        selected = state.type == t,
                        onClick = { viewModel.onTypeChange(t) },
                        shape = SegmentedButtonDefaults.itemShape(index, 2),
                        colors = com.omer.expensetracker.presentation.components.themedSegmentedColors()
                    ) {
                        Text(if (t == EntryType.EXPENSE) "Expense" else "Income")
                    }
                }
            }

            AmountInputField(
                value = TextFieldValue(state.amountText, selection = TextRange(state.amountText.length)),
                onValueChange = { viewModel.onAmountChange(it.text) },
                errorMessage = state.errorMessage,
                modifier = Modifier.padding(top = 16.dp)
            )

            if (state.type == EntryType.EXPENSE) {
                Text("Category", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
                CategoryPickerGrid(
                    categories = state.categories,
                    selectedCategoryId = state.categoryId,
                    onSelect = { viewModel.onCategorySelect(it.id) }
                )
            }

            Text("Repeats every", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.intervalCountText,
                    onValueChange = viewModel::onIntervalCountChange,
                    label = { Text("N") },
                    modifier = Modifier.weight(1f)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(2f)) {
                    listOf(RecurrenceUnit.DAY, RecurrenceUnit.WEEK, RecurrenceUnit.MONTH).forEachIndexed { index, u ->
                        SegmentedButton(
                            selected = state.unit == u,
                            onClick = { viewModel.onUnitChange(u) },
                            shape = SegmentedButtonDefaults.itemShape(index, 3),
                            colors = com.omer.expensetracker.presentation.components.themedSegmentedColors()
                        ) {
                            Text(u.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }

            DatePickerField(
                date = state.startDate,
                onDateChange = viewModel::onStartDateChange,
                modifier = Modifier.padding(top = 16.dp)
            )

            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("Set an end date", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                androidx.compose.material3.Switch(
                    checked = state.endDate != null,
                    onCheckedChange = viewModel::onHasEndDateToggle
                )
            }
            val endDate = state.endDate
            if (endDate != null) {
                DatePickerField(
                    date = endDate,
                    onDateChange = viewModel::onEndDateChange,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            com.omer.expensetracker.presentation.components.GradientButton(
                text = "Save",
                onClick = viewModel::save,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}
