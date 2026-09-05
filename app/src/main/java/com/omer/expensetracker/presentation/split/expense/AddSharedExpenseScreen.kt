package com.omer.expensetracker.presentation.split.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.presentation.components.AmountInputField
import com.omer.expensetracker.presentation.components.ConfirmDeleteDialog
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.FieldLabel
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.split.components.FriendAvatar
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass

private fun displayName(friend: Friend) = if (friend.isYou) "You" else friend.name

private fun splitModeLabel(mode: SplitMode) = when (mode) {
    SplitMode.EQUAL -> "Equal"
    SplitMode.EXACT -> "Exact"
    SplitMode.PERCENTAGE -> "%"
    SplitMode.SHARES -> "Shares"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSharedExpenseScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddSharedExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved, state.isDeleted) { if (state.isSaved || state.isDeleted) onDone() }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete expense?",
            message = "This reverses its effect on every balance it touched. This can't be undone.",
            onConfirm = { showDeleteConfirm = false; viewModel.delete() },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditing) "Edit expense" else "Add expense", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    if (state.isEditing) {
                        TonalIconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete expense")
                        }
                    }
                },
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
            FieldLabel("Description")
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                singleLine = true,
                placeholder = { Text("Dinner at Rosa's") },
                modifier = Modifier.fillMaxWidth()
            )

            FieldLabel("Amount", modifier = Modifier.padding(top = 20.dp))
            AmountInputField(
                value = androidx.compose.ui.text.input.TextFieldValue(state.amountText, selection = androidx.compose.ui.text.TextRange(state.amountText.length)),
                onValueChange = { viewModel.onAmountChange(it.text) },
                errorMessage = state.errorMessage
            )

            FieldLabel("Date", modifier = Modifier.padding(top = 20.dp))
            DatePickerField(date = state.date, onDateChange = viewModel::onDateChange)

            FieldLabel("Paid by", modifier = Modifier.padding(top = 22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                state.availableParticipants.forEach { friend ->
                    PersonChip(friend, selected = friend.id in state.selectedPayerIds, onClick = { viewModel.togglePayer(friend.id) })
                }
            }
            if (state.selectedPayerIds.size > 1) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    state.availableParticipants.filter { it.id in state.selectedPayerIds }.forEach { friend ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(displayName(friend), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            SmallField(
                                value = state.payerAmountText[friend.id] ?: "",
                                onValueChange = { viewModel.onPayerAmountChange(friend.id, it) },
                                placeholder = "0.00"
                            )
                        }
                    }
                }
            }

            FieldLabel("Split", modifier = Modifier.padding(top = 22.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SplitMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = state.splitMode == mode,
                        onClick = { viewModel.onSplitModeSelect(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, SplitMode.entries.size),
                        colors = com.omer.expensetracker.presentation.components.themedSegmentedColors()
                    ) {
                        Text(splitModeLabel(mode), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
            }

            FieldLabel("Split between", modifier = Modifier.padding(top = 22.dp))
            Column {
                state.availableParticipants.forEach { friend ->
                    val selected = friend.id in state.selectedParticipantIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (selected) MaterialTheme.colorScheme.primary else SurfaceGlass)
                                .border(1.dp, BorderGlass, CircleShape)
                                .clickable { viewModel.toggleParticipant(friend.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        FriendAvatar(displayName(friend), friend.avatarColorArgb, size = 34.dp)
                        Text(displayName(friend), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        if (selected) {
                            when (state.splitMode) {
                                SplitMode.EXACT -> SmallField(
                                    value = state.exactAmountText[friend.id] ?: "",
                                    onValueChange = { viewModel.onExactAmountChange(friend.id, it) },
                                    placeholder = "0.00"
                                )
                                SplitMode.PERCENTAGE -> SmallField(
                                    value = state.percentageText[friend.id] ?: "",
                                    onValueChange = { viewModel.onPercentageChange(friend.id, it) },
                                    placeholder = "%"
                                )
                                SplitMode.SHARES -> SmallField(
                                    value = state.sharesText[friend.id] ?: "",
                                    onValueChange = { viewModel.onSharesChange(friend.id, it) },
                                    placeholder = "1"
                                )
                                SplitMode.EQUAL -> Unit
                            }
                        }
                    }
                }
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }

            GradientButton(
                text = if (state.isEditing) "Save changes" else "Add expense",
                onClick = viewModel::save,
                modifier = Modifier.padding(top = 26.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun PersonChip(friend: Friend, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .border(2.5.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        FriendAvatar(displayName(friend), friend.avatarColorArgb, size = 40.dp)
        Text(
            displayName(friend),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SmallField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' }) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.width(84.dp)
    )
}
