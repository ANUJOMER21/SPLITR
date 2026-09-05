package com.omer.expensetracker.presentation.split.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.components.AmountInputField
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.FieldLabel
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.components.themedSegmentedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettleUpViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) { if (state.isSaved) onDone() }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settle up", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            val friendName = state.friend?.name ?: "friend"

            FieldLabel("Who's paying")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.friendIsPaying,
                    onClick = { viewModel.onDirectionSelect(true) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    colors = themedSegmentedColors()
                ) { Text("$friendName pays you") }
                SegmentedButton(
                    selected = !state.friendIsPaying,
                    onClick = { viewModel.onDirectionSelect(false) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    colors = themedSegmentedColors()
                ) { Text("You pay $friendName") }
            }

            FieldLabel("Amount", modifier = Modifier.padding(top = 20.dp))
            AmountInputField(
                value = TextFieldValue(state.amountText, selection = TextRange(state.amountText.length)),
                onValueChange = { viewModel.onAmountChange(it.text) },
                errorMessage = state.errorMessage
            )

            FieldLabel("Date", modifier = Modifier.padding(top = 20.dp))
            DatePickerField(date = state.date, onDateChange = viewModel::onDateChange)

            FieldLabel("Note (optional)", modifier = Modifier.padding(top = 20.dp))
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            GradientButton(text = "Record settlement", onClick = viewModel::save, modifier = Modifier.padding(top = 26.dp, bottom = 24.dp))
        }
    }
}
