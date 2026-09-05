package com.omer.expensetracker.presentation.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.BudgetProgress
import com.omer.expensetracker.domain.model.BudgetStatus
import com.omer.expensetracker.presentation.components.AnimatedLinearProgress
import com.omer.expensetracker.presentation.components.CategoryDot
import com.omer.expensetracker.presentation.components.ConfirmDeleteDialog
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.ExpenseRed
import com.omer.expensetracker.ui.theme.IncomeGreen
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onAddBudget: () -> Unit,
    onEditBudget: (categoryIdOrSentinel: String) -> Unit,
    onBack: () -> Unit,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Budgets") },
                navigationIcon = {
                    TonalIconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            com.omer.expensetracker.presentation.components.SolidFab(onClick = onAddBudget, contentDescription = "Add budget")
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            com.omer.expensetracker.presentation.components.MonthSwitcher(
                monthLabel = state.month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (state.budgets.isEmpty() && !state.isLoading) {
                EmptyState(title = "No budgets yet", subtitle = "Set a monthly limit for a category to track it here")
            } else {
                LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)) {
                    items(state.budgets, key = { it.budget.id }) { progress ->
                        BudgetRow(
                            progress,
                            onClick = { onEditBudget(progress.budget.categoryId ?: OVERALL_BUDGET_SENTINEL) },
                            onLongClick = { viewModel.requestDelete(progress.budget.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        ConfirmDeleteDialog(
            title = "Delete budget?",
            message = "This only removes the limit — your expenses are untouched.",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
private fun BudgetRow(progress: BudgetProgress, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    val barColor = when (progress.status) {
        BudgetStatus.SAFE -> IncomeGreen
        BudgetStatus.WARNING -> com.omer.expensetracker.ui.theme.WarningAmber
        BudgetStatus.BREACHED -> ExpenseRed
    }
    ListItemCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (progress.category != null) {
                CategoryDot(progress.category.colorArgb, progress.category.iconKey, size = 32.dp)
            }
            Text(
                progress.category?.name ?: "Overall",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${progress.spentMinor.formatAsCurrency()} / ${progress.limitMinor.formatAsCurrency()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedLinearProgress(
            fraction = progress.fraction,
            color = barColor,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}
