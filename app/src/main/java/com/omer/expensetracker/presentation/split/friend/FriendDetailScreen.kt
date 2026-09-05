package com.omer.expensetracker.presentation.split.friend

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.components.ConfirmDeleteDialog
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.GlassButton
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.split.components.FriendAvatar
import com.omer.expensetracker.presentation.split.components.netMinorColor
import com.omer.expensetracker.presentation.split.components.netMinorLabel
import com.omer.expensetracker.presentation.util.formatAsCurrency
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    onBack: () -> Unit,
    onSettleUp: (String) -> Unit,
    viewModel: FriendDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.reminderSent, state.reminderError) {
        if (state.reminderSent || state.reminderError != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearReminderState()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.friend?.name ?: "Friend", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    TonalIconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete friend") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        val friend = state.friend
        if (friend == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                FriendAvatar(friend.name, friend.avatarColorArgb, size = 56.dp)
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(friend.name, style = MaterialTheme.typography.titleLarge)
                    Text(netMinorLabel(state.netMinor), style = MaterialTheme.typography.bodyLarge, color = netMinorColor(state.netMinor))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 20.dp)) {
                GlassButton(text = "Settle up", onClick = { onSettleUp(friend.id) }, icon = Icons.Filled.SwapHoriz, modifier = Modifier.weight(1f))
                if (state.netMinor > 0L) {
                    GlassButton(text = "Remind", onClick = viewModel::sendSettlementReminder, icon = Icons.Filled.NotificationsActive, modifier = Modifier.weight(1f))
                }
            }

            if (state.reminderSent) {
                Text("Reminder sent", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }
            state.reminderError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }

            Text("History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 26.dp, bottom = 10.dp))

            if (state.ledger.isEmpty()) {
                EmptyState(title = "No history yet", subtitle = "Shared expenses and settlements with ${friend.name} show up here")
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(state.ledger, key = {
                        when (it) {
                            is FriendLedgerEntry.ExpenseEntry -> "e:${it.expense.id}"
                            is FriendLedgerEntry.SettlementEntry -> "s:${it.settlement.id}"
                        }
                    }) { entry ->
                        ListItemCard(onClick = {}, modifier = Modifier.animateItem(), paddingValues = PaddingValues(vertical = 6.dp)) {
                            when (entry) {
                                is FriendLedgerEntry.ExpenseEntry -> {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(entry.expense.description, style = MaterialTheme.typography.bodyLarge)
                                        Text(entry.expense.amountMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Text(
                                        entry.expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                is FriendLedgerEntry.SettlementEntry -> {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            if (entry.settlement.payerFriendId == friend.id) "${friend.name} paid you" else "You paid ${friend.name}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(entry.settlement.amountMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Text(
                                        entry.settlement.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete friend?",
            message = "Their balance and history stay on record, but they'll no longer appear in your friends list.",
            onConfirm = { showDeleteConfirm = false; viewModel.deleteFriend(onBack) },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
