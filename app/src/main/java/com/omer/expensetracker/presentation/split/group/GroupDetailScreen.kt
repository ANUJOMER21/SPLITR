package com.omer.expensetracker.presentation.split.group

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.split.components.FriendAvatar
import com.omer.expensetracker.presentation.split.components.netMinorColor
import com.omer.expensetracker.presentation.split.components.netMinorLabel
import com.omer.expensetracker.presentation.util.formatAsCurrency
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBack: () -> Unit,
    onAddExpense: (String) -> Unit,
    onEditExpense: (groupId: String, expenseId: String) -> Unit,
    onSimplify: (String) -> Unit,
    onSettleUpWithFriend: (friendId: String, groupId: String) -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.group?.name ?: "Group", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    TonalIconButton(onClick = { onSimplify(viewModel.groupId) }) { Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = "Simplify debts") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            com.omer.expensetracker.presentation.components.SolidFab(onClick = { onAddExpense(viewModel.groupId) }, contentDescription = "Add expense")
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.tab.ordinal, containerColor = Color.Transparent) {
                GroupDetailTab.entries.forEach { tab ->
                    Tab(
                        selected = state.tab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            when (state.tab) {
                GroupDetailTab.EXPENSES -> ExpensesTab(state) { expenseId -> onEditExpense(viewModel.groupId, expenseId) }
                GroupDetailTab.BALANCES -> BalancesTab(state) { friendId -> onSettleUpWithFriend(friendId, viewModel.groupId) }
                GroupDetailTab.ACTIVITY -> ActivityTab(state)
            }
        }
    }
}

@Composable
private fun ExpensesTab(state: GroupDetailUiState, onEditExpense: (expenseId: String) -> Unit) {
    if (state.expenses.isEmpty() && !state.isLoading) {
        EmptyState(title = "No expenses yet", subtitle = "Add the first shared expense for this group")
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        items(state.expenses, key = { it.id }) { expense ->
            ListItemCard(onClick = { onEditExpense(expense.id) }, modifier = Modifier.animateItem()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(expense.description, style = MaterialTheme.typography.bodyLarge)
                    Text(expense.amountMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun BalancesTab(state: GroupDetailUiState, onSettleUp: (friendId: String) -> Unit) {
    if (state.members.isEmpty() && !state.isLoading) {
        EmptyState(title = "No members yet", subtitle = "Add friends to this group to track balances")
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        items(state.members, key = { it.friend.id }) { member ->
            ListItemCard(onClick = { onSettleUp(member.friend.id) }, modifier = Modifier.animateItem()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FriendAvatar(member.friend.name, member.friend.avatarColorArgb)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(member.friend.name, style = MaterialTheme.typography.bodyLarge)
                        Text(netMinorLabel(member.netMinor), style = MaterialTheme.typography.bodyMedium, color = netMinorColor(member.netMinor))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityTab(state: GroupDetailUiState) {
    if (state.activity.isEmpty() && !state.isLoading) {
        EmptyState(title = "No activity yet", subtitle = "Expenses and settlements in this group will show up here")
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 10.dp)) {
        items(state.activity, key = { it.id }) { entry ->
            ListItemCard(onClick = {}, modifier = Modifier.animateItem()) {
                Text(entry.summary, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
