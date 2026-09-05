package com.omer.expensetracker.presentation.split.expense

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.omer.expensetracker.presentation.components.themedSegmentedColors
import com.omer.expensetracker.presentation.split.components.netMinorColor
import com.omer.expensetracker.presentation.split.components.netMinorLabel
import com.omer.expensetracker.presentation.util.formatAsCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifyDebtsScreen(
    onBack: () -> Unit,
    viewModel: SimplifyDebtsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Simplify debts", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                SegmentedButton(
                    selected = state.showSimplified,
                    onClick = { if (!state.showSimplified) viewModel.toggleView() },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    colors = themedSegmentedColors()
                ) { Text("Simplified") }
                SegmentedButton(
                    selected = !state.showSimplified,
                    onClick = { if (state.showSimplified) viewModel.toggleView() },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    colors = themedSegmentedColors()
                ) { Text("Raw balances") }
            }

            if (state.showSimplified) {
                if (state.simplifiedPayments.isEmpty() && !state.isLoading) {
                    EmptyState(title = "Everyone's settled up", subtitle = "No payments needed to zero out this group")
                } else {
                    LazyColumn(contentPadding = PaddingValues(vertical = 14.dp)) {
                        items(state.simplifiedPayments) { payment ->
                            ListItemCard(onClick = {}, modifier = Modifier.animateItem()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(payment.fromName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(payment.toName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                }
                                Text(
                                    payment.amountMinor.formatAsCurrency(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                if (state.rawBalances.isEmpty() && !state.isLoading) {
                    EmptyState(title = "Everyone's settled up", subtitle = "No outstanding balances in this group")
                } else {
                    LazyColumn(contentPadding = PaddingValues(vertical = 14.dp)) {
                        items(state.rawBalances) { (name, net) ->
                            ListItemCard(onClick = {}, modifier = Modifier.animateItem()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(name, style = MaterialTheme.typography.bodyLarge)
                                    Text(netMinorLabel(net), color = netMinorColor(net), style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
