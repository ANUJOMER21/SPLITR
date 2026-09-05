package com.omer.expensetracker.presentation.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.domain.model.AnomalyFlag
import com.omer.expensetracker.domain.model.HeatmapCell
import com.omer.expensetracker.domain.model.InsightsPeriod
import com.omer.expensetracker.domain.model.MonthOverMonthChange
import com.omer.expensetracker.domain.model.TopCategoryInsight
import com.omer.expensetracker.presentation.components.AnimatedCurrencyText
import com.omer.expensetracker.presentation.components.CategoryDot
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.pressScale
import com.omer.expensetracker.presentation.split.components.FriendAvatar
import com.omer.expensetracker.presentation.split.components.netMinorColor
import com.omer.expensetracker.presentation.split.components.netMinorLabel
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.ExpenseRed
import com.omer.expensetracker.ui.theme.HeroGradientEnd
import com.omer.expensetracker.ui.theme.HeroGradientStart
import com.omer.expensetracker.ui.theme.IncomeGreen
import com.omer.expensetracker.ui.theme.OnHeroGradientMuted
import com.omer.expensetracker.ui.theme.OnHeroGradientPrimary
import com.omer.expensetracker.ui.theme.SurfaceGlass
import com.omer.expensetracker.ui.theme.WarningAmber
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val visibleTabs = InsightsTab.entries.filter { it != InsightsTab.SHARED || FeatureFlags.SPLIT_WITH_FRIENDS_ENABLED }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("Insights", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            PillTabRow(
                tabs = visibleTabs,
                selected = state.tab,
                onSelect = viewModel::selectTab,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            when (state.tab) {
                InsightsTab.TREND -> TrendTab(state, viewModel::selectPeriod)
                InsightsTab.TOP_CATEGORIES -> TopCategoriesTab(state.topCategories)
                InsightsTab.HEATMAP -> HeatmapTab(state.heatmap)
                InsightsTab.INCOME_VS_EXPENSE -> IncomeVsExpenseTab(state)
                InsightsTab.SAVINGS_RATE -> SavingsRateTab(state, viewModel::selectPeriod)
                InsightsTab.ANOMALIES -> AnomaliesTab(state.anomalies)
                InsightsTab.SHARED -> SharedTab(state.sharedBalances)
            }
        }
    }
}

private fun InsightsTab.label(): String = when (this) {
    InsightsTab.TREND -> "Trend"
    InsightsTab.TOP_CATEGORIES -> "Top categories"
    InsightsTab.HEATMAP -> "Heatmap"
    InsightsTab.INCOME_VS_EXPENSE -> "Income vs expense"
    InsightsTab.SAVINGS_RATE -> "Savings rate"
    InsightsTab.ANOMALIES -> "Notable"
    InsightsTab.SHARED -> "Shared"
}

/** A horizontally scrollable row of glass pill tabs — matches the segmented-button / chip
 * language used everywhere else in the app, instead of the stock Material underline tabs. */
@Composable
private fun PillTabRow(
    tabs: List<InsightsTab>,
    selected: InsightsTab,
    onSelect: (InsightsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs) { tab ->
            val isSelected = tab == selected
            val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Surface(
                onClick = { onSelect(tab) },
                interactionSource = interactionSource,
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color.Transparent else SurfaceGlass,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                modifier = Modifier.pressScale(interactionSource)
            ) {
                Box(
                    modifier = if (isSelected) {
                        Modifier.background(Brush.linearGradient(listOf(AccentBlueDeep, AccentBlue)))
                    } else Modifier
                ) {
                    Text(
                        tab.label(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(period: InsightsPeriod, onSelect: (InsightsPeriod) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        InsightsPeriod.entries.forEachIndexed { index, p ->
            SegmentedButton(
                selected = period == p,
                onClick = { onSelect(p) },
                shape = SegmentedButtonDefaults.itemShape(index, InsightsPeriod.entries.size),
                colors = com.omer.expensetracker.presentation.components.themedSegmentedColors()
            ) {
                Text(p.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

/** A compact gradient hero, shared by every tab that leads with one headline number. */
@Composable
private fun InsightHeroCard(label: String, amountMinor: Long, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = OnHeroGradientMuted)
            AnimatedCurrencyText(
                amountMinor = amountMinor,
                style = MaterialTheme.typography.headlineMedium,
                color = OnHeroGradientPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ChartCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun TrendTab(state: InsightsUiState, onPeriodSelect: (InsightsPeriod) -> Unit) {
    Column {
        PeriodSelector(state.period, onPeriodSelect)
        val total = state.trend.sumOf { it.expenseMinor }
        InsightHeroCard(label = "Total spent", amountMinor = total)
        if (state.trend.all { it.expenseMinor == 0L }) {
            EmptyState(title = "No spending yet", subtitle = "Your spend trend will appear here")
        } else {
            ChartCard {
                SimpleBarChart(data = state.trend.map { BarDatum(it.periodLabel, it.expenseMinor / 100f) }, barColor = ExpenseRed)
            }
        }
    }
}

@Composable
private fun TopCategoriesTab(items: List<TopCategoryInsight>) {
    if (items.isEmpty()) {
        EmptyState(title = "No expenses this month", subtitle = "Top categories will rank here")
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        items(items, key = { it.category.id }) { item ->
            ListItemCard(onClick = {}, modifier = Modifier.animateItem(), paddingValues = PaddingValues(vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryDot(item.category.colorArgb, item.category.iconKey, size = 40.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.category.name, style = MaterialTheme.typography.bodyLarge)
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((item.percentage / 100f).coerceIn(0f, 1f))
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(item.category.colorArgb))
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 4.dp)) {
                        Text(item.totalMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyLarge)
                        Text("${"%.0f".format(item.percentage)}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapTab(cells: List<HeatmapCell>) {
    val maxValue = cells.maxOfOrNull { it.totalMinor }?.coerceAtLeast(1L) ?: 1L
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
        items(cells.sortedBy { it.dayOfWeek }, key = { it.dayOfWeek }) { cell ->
            val dayName = DayOfWeek.of(cell.dayOfWeek).getDisplayName(TextStyle.FULL, Locale.getDefault())
            ListItemCard(onClick = {}, modifier = Modifier.fillMaxWidth().animateItem(), paddingValues = PaddingValues(vertical = 6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceGlass),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(dayName.take(1), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(dayName, modifier = Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
                    Text(cell.totalMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((cell.totalMinor.toFloat() / maxValue).coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Brush.horizontalGradient(listOf(AccentBlueDeep, AccentBlue)))
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeVsExpenseTab(state: InsightsUiState) {
    Column(modifier = Modifier.fillMaxSize()) {
        state.overallMoM?.let { overall ->
            val change = overall.percentageChange
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("THIS MONTH VS LAST", style = MaterialTheme.typography.labelSmall, color = OnHeroGradientMuted)
                    Text(
                        if (change != null) "${"%+.0f".format(change)}%" else "No prior month to compare",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnHeroGradientPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.categoryMoM, key = { it.category?.id ?: "none" }) { change ->
                MonthOverMonthRow(change)
            }
        }
    }
}

@Composable
private fun MonthOverMonthRow(change: MonthOverMonthChange) {
    ListItemCard(onClick = {}, paddingValues = PaddingValues(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            change.category?.let { CategoryDot(it.colorArgb, it.iconKey, size = 36.dp) }
            Text(change.category?.name ?: "Unknown", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(change.currentMonthMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyLarge)
            val pct = change.percentageChange
            Text(
                pct?.let { "${"%+.0f".format(it)}%" } ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                color = if ((pct ?: 0f) > 0) ExpenseRed else IncomeGreen,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun SavingsRateTab(state: InsightsUiState, onPeriodSelect: (InsightsPeriod) -> Unit) {
    Column {
        PeriodSelector(state.period, onPeriodSelect)
        val latest = state.savingsRate.lastOrNull()?.rate
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("LATEST SAVINGS RATE", style = MaterialTheme.typography.labelSmall, color = OnHeroGradientMuted)
                Text(
                    latest?.let { "${"%.0f".format(it * 100)}%" } ?: "—",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnHeroGradientPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        ChartCard {
            SimpleBarChart(
                data = state.savingsRate.map { BarDatum(it.periodLabel, ((it.rate ?: 0f) * 100).coerceAtLeast(0f)) },
                barColor = IncomeGreen
            )
        }
    }
}

@Composable
private fun AnomaliesTab(anomalies: List<AnomalyFlag>) {
    if (anomalies.isEmpty()) {
        EmptyState(title = "Nothing unusual", subtitle = "Expenses well above your category average show up here")
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        items(anomalies, key = { it.entry.id }) { flag ->
            ListItemCard(onClick = {}, modifier = Modifier.animateItem(), paddingValues = PaddingValues(vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(WarningAmber.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PriorityHigh, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(flag.category?.name ?: "Other", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Usually ~${flag.categoryAverageMinor.formatAsCurrency()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(flag.entry.amountMinor.formatAsCurrency(), style = MaterialTheme.typography.bodyLarge, color = ExpenseRed)
                }
            }
        }
    }
}

@Composable
private fun SharedTab(balances: List<SharedFriendBalance>) {
    if (balances.isEmpty()) {
        EmptyState(title = "No friends yet", subtitle = "Balances with friends and groups will summarize here")
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        items(balances, key = { it.friendId }) { balance ->
            ListItemCard(onClick = {}, modifier = Modifier.animateItem(), paddingValues = PaddingValues(vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FriendAvatar(balance.name, balance.colorArgb, size = 36.dp)
                    Text(balance.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(netMinorLabel(balance.netMinor), style = MaterialTheme.typography.bodyMedium, color = netMinorColor(balance.netMinor))
                }
            }
        }
    }
}
