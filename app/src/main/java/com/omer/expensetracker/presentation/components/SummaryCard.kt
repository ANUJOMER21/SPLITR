package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.domain.model.MonthlySummary
import com.omer.expensetracker.ui.theme.HeroGradientEnd
import com.omer.expensetracker.ui.theme.HeroGradientStart
import com.omer.expensetracker.ui.theme.IncomeGreenContainer
import com.omer.expensetracker.ui.theme.OnHeroGradientMuted
import com.omer.expensetracker.ui.theme.OnHeroGradientPrimary
import com.omer.expensetracker.ui.theme.ExpenseRedContainer

@Composable
fun SummaryCard(summary: MonthlySummary, modifier: Modifier = Modifier) {
    val negative = ExpenseRedContainer
    val positive = IncomeGreenContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "NET BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnHeroGradientMuted
                    )
                    AnimatedCurrencyText(
                        amountMinor = summary.netMinor,
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (summary.netMinor >= 0) OnHeroGradientPrimary else negative,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = OnHeroGradientMuted,
                    modifier = Modifier
                )
            }

            HorizontalDivider(color = OnHeroGradientMuted.copy(alpha = 0.3f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn("Expense", summary.totalExpenseMinor, negative)
                SummaryColumn("Income", summary.totalIncomeMinor, positive)
            }
        }
    }
}

@Composable
private fun SummaryColumn(label: String, amountMinor: Long, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnHeroGradientMuted)
        AnimatedCurrencyText(
            amountMinor = amountMinor,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
