package com.omer.expensetracker.presentation.goal

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.omer.expensetracker.presentation.components.AmountInputField
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.pressScale
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditGoalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) { if (state.isSaved) onDone() }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New savings goal") },
                navigationIcon = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Goal name") },
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
                date = state.targetDate ?: LocalDate.now().plusMonths(6),
                onDateChange = viewModel::onTargetDateChange,
                modifier = Modifier.padding(top = 16.dp)
            )

            com.omer.expensetracker.presentation.components.GradientButton(
                text = "Create goal",
                onClick = viewModel::save,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}
