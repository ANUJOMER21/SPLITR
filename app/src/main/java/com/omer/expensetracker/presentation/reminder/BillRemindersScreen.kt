package com.omer.expensetracker.presentation.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.presentation.components.ConfirmDeleteDialog
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.ExpenseRed
import com.omer.expensetracker.ui.theme.IncomeGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillRemindersScreen(
    onAddReminder: () -> Unit,
    onEditReminder: (id: String) -> Unit,
    onBack: () -> Unit,
    viewModel: BillRemindersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bill reminders") },
                navigationIcon = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder) {
                Icon(Icons.Filled.Add, contentDescription = "Add reminder")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.reminders.isEmpty() && !state.isLoading) {
                EmptyState(title = "No reminders", subtitle = "Add a due date so bills never sneak up on you")
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(state.reminders, key = { it.id }) { reminder ->
                        ReminderRow(
                            reminder = reminder,
                            categoryName = reminder.categoryId?.let { state.categoriesById[it]?.name },
                            onClick = { onEditReminder(reminder.id) },
                            onLongClick = { viewModel.requestDelete(reminder.id) },
                            onMarkPaid = { viewModel.markPaid(reminder.id, autoCreateEntry = true) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        ConfirmDeleteDialog(
            title = "Delete reminder?",
            message = "This reminder will be removed.",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
private fun ReminderRow(
    reminder: BillReminder,
    categoryName: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMarkPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overdue = reminder.isOverdueUnpaid(LocalDate.now())
    ListItemCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(reminder.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                buildString {
                    append("Due ${reminder.dueDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
                    categoryName?.let { append(" · $it") }
                    if (overdue) append(" · Overdue")
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (overdue) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        reminder.estimatedAmountMinor?.let {
            Text(it.formatAsCurrency(), style = MaterialTheme.typography.bodyLarge)
        }
        if (reminder.isPaid) {
            Icon(Icons.Filled.CheckCircle, contentDescription = "Paid", tint = IncomeGreen)
        } else {
            IconButton(onClick = onMarkPaid) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Mark paid")
            }
        }
    }
    }
}
