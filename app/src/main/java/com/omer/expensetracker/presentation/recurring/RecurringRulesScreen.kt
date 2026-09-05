package com.omer.expensetracker.presentation.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.presentation.components.ConfirmDeleteDialog
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.util.formatAsCurrency
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringRulesScreen(
    onAddRule: () -> Unit,
    onEditRule: (id: String) -> Unit,
    onBack: () -> Unit,
    viewModel: RecurringRulesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recurring") },
                navigationIcon = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        floatingActionButton = {
            com.omer.expensetracker.presentation.components.SolidFab(onClick = onAddRule, contentDescription = "Add recurring rule")
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.rules.isEmpty() && !state.isLoading) {
                EmptyState(title = "No recurring entries", subtitle = "Bills or income that repeat show up here")
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(state.rules, key = { it.id }) { rule ->
                        RecurringRuleRow(
                            rule = rule,
                            categoryName = rule.categoryId?.let { state.categoriesById[it]?.name },
                            onClick = { onEditRule(rule.id) },
                            onTogglePaused = { viewModel.togglePaused(rule.id, it) },
                            onSkipNext = { viewModel.skipNext(rule.id) },
                            onDelete = { viewModel.requestDelete(rule.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        ConfirmDeleteDialog(
            title = "Delete recurring rule?",
            message = "Entries already generated from it are kept.",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
private fun RecurringRuleRow(
    rule: RecurringRule,
    categoryName: String?,
    onClick: () -> Unit,
    onTogglePaused: (Boolean) -> Unit,
    onSkipNext: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    ListItemCard(
        onClick = onClick,
        onLongClick = { showMenu = true },
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                categoryName ?: if (rule.type == EntryType.EXPENSE) "Expense" else "Income",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "${rule.unit.name.lowercase().replaceFirstChar { it.uppercase() }} × ${rule.intervalCount} · next ${rule.nextDueDate.format(DateTimeFormatter.ofPattern("dd MMM"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(rule.amountMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = !rule.isPaused, onCheckedChange = { onTogglePaused(!it) })

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "More actions")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Skip next occurrence") }, onClick = { onSkipNext(); showMenu = false })
                DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); showMenu = false })
            }
        }
    }
    }
}
