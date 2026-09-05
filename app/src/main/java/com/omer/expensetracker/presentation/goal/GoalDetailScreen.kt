package com.omer.expensetracker.presentation.goal

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.GoalContribution
import com.omer.expensetracker.presentation.components.AmountInputField
import com.omer.expensetracker.presentation.components.AnimatedCurrencyText
import com.omer.expensetracker.presentation.components.AnimatedLinearProgress
import com.omer.expensetracker.presentation.components.ConfirmDeleteDialog
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.HeroGradientEnd
import com.omer.expensetracker.ui.theme.HeroGradientStart
import com.omer.expensetracker.ui.theme.IncomeGreen
import com.omer.expensetracker.ui.theme.IncomeGreenContainer
import com.omer.expensetracker.ui.theme.OnHeroGradientMuted
import com.omer.expensetracker.ui.theme.OnHeroGradientPrimary
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val detail = state.detail

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(detail?.goal?.name ?: "Goal") },
                navigationIcon = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        floatingActionButton = {
            com.omer.expensetracker.presentation.components.SolidFab(onClick = viewModel::openAddContribution, contentDescription = "Log contribution")
        }
    ) { padding ->
        if (detail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                        .padding(20.dp)
                ) {
                    AnimatedCurrencyText(
                        amountMinor = detail.savedMinor,
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnHeroGradientPrimary
                    )
                    Text(
                        "of ${detail.goal.targetAmountMinor.formatAsCurrency()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnHeroGradientMuted
                    )
                    Text(
                        "${(detail.fraction * 100).toInt()}% · ${detail.remainingMinor.formatAsCurrency()} remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnHeroGradientMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    detail.isOnTrack?.let { onTrack ->
                        Text(
                            if (onTrack) "On track to hit your target date" else "Behind pace for your target date",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (onTrack) IncomeGreenContainer else OnHeroGradientPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    AnimatedLinearProgress(
                        fraction = detail.fraction,
                        color = IncomeGreen,
                        trackColor = OnHeroGradientMuted.copy(alpha = 0.25f),
                        height = 10.dp,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }

            Text(
                "Contributions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(detail.contributions, key = { it.id }) { contribution ->
                    ContributionRow(
                        contribution,
                        onLongClick = { viewModel.requestDeleteContribution(contribution.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (state.showAddContribution) {
        AlertDialog(
            onDismissRequest = viewModel::dismissAddContribution,
            containerColor = com.omer.expensetracker.ui.theme.BackgroundBase,
            title = { Text("Log contribution") },
            text = {
                Column {
                    AmountInputField(
                        value = TextFieldValue(state.contributionAmountText, selection = TextRange(state.contributionAmountText.length)),
                        onValueChange = { viewModel.onContributionAmountChange(it.text) },
                        errorMessage = state.errorMessage
                    )
                    DatePickerField(
                        date = state.contributionDate,
                        onDateChange = viewModel::onContributionDateChange,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveContribution) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissAddContribution) { Text("Cancel") }
            }
        )
    }

    if (state.pendingDeleteContributionId != null) {
        ConfirmDeleteDialog(
            title = "Delete contribution?",
            message = "This will reduce the goal's saved total.",
            onConfirm = viewModel::confirmDeleteContribution,
            onDismiss = viewModel::cancelDeleteContribution
        )
    }
}

@Composable
private fun ContributionRow(contribution: GoalContribution, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItemCard(onClick = {}, onLongClick = onLongClick, modifier = modifier, paddingValues = PaddingValues(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(contribution.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")), style = MaterialTheme.typography.bodyMedium)
            Text(contribution.amountMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyMedium, color = IncomeGreen)
        }
    }
}
