package com.omer.expensetracker.presentation.goal

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
import com.omer.expensetracker.domain.model.SavingsGoalDetail
import com.omer.expensetracker.presentation.components.AnimatedLinearProgress
import com.omer.expensetracker.presentation.components.ConfirmDeleteDialog
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onAddGoal: () -> Unit,
    onOpenGoal: (id: String) -> Unit,
    onBack: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Savings goals") },
                navigationIcon = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGoal) {
                Icon(Icons.Filled.Add, contentDescription = "Add goal")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.goals.isEmpty() && !state.isLoading) {
                EmptyState(title = "No savings goals", subtitle = "Set a target and start logging contributions")
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(state.goals, key = { it.goal.id }) { detail ->
                        GoalRow(
                            detail = detail,
                            onClick = { onOpenGoal(detail.goal.id) },
                            onLongClick = { viewModel.requestDelete(detail.goal.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        ConfirmDeleteDialog(
            title = "Delete goal?",
            message = "Its contribution history will be removed too.",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
private fun GoalRow(detail: SavingsGoalDetail, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItemCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(detail.goal.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (detail.goal.isCompleted) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Completed", tint = IncomeGreen)
            }
        }
        Text(
            "${detail.savedMinor.formatAsCurrency()} of ${detail.goal.targetAmountMinor.formatAsCurrency()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedLinearProgress(
            fraction = detail.fraction,
            color = IncomeGreen,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}
