package com.omer.expensetracker.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.omer.expensetracker.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.BudgetProgress
import com.omer.expensetracker.domain.model.BudgetStatus
import com.omer.expensetracker.domain.model.CategoryBreakdownItem
import com.omer.expensetracker.domain.model.MonthOverMonthChange
import com.omer.expensetracker.presentation.components.CategoryBreakdownChart
import com.omer.expensetracker.presentation.components.EntryListRow
import com.omer.expensetracker.presentation.components.GlassButton
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.presentation.components.GradientIconBadge
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.MonthSwitcher
import com.omer.expensetracker.presentation.split.components.netMinorColor
import com.omer.expensetracker.presentation.split.components.netMinorLabel
import com.omer.expensetracker.presentation.util.CategoryIconProvider
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.ui.theme.BadgeGradients
import com.omer.expensetracker.ui.theme.ExpenseRed
import com.omer.expensetracker.ui.theme.HeroGradientEnd
import com.omer.expensetracker.ui.theme.HeroGradientStart
import com.omer.expensetracker.ui.theme.IncomeGreen
import com.omer.expensetracker.ui.theme.OnHeroGradientMuted
import com.omer.expensetracker.ui.theme.OnHeroGradientPrimary
import com.omer.expensetracker.ui.theme.WarningAmber
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onCategoryClick: (categoryId: String) -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenFriends: () -> Unit = {},
    onOpenGroups: () -> Unit = {},
    onEntryClick: (entryId: String) -> Unit = {},
    onViewAllEntries: () -> Unit = {},
    onOpenBudgets: () -> Unit = {},
    onOpenRecurring: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
    onOpenGoals: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Image(
//                            painter = painterResource(R.drawable.logo),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .size(36.dp)
//                                .clip(RoundedCornerShape(10.dp))
//                        )
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "Splitr",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    letterSpacing = 0.2.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Expense Tracker",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            MonthSwitcher(
                monthLabel = state.month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GradientButton(
                    text = "Add Expense",
                    onClick = onAddExpense,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Add Income",
                    onClick = onAddIncome,
                    icon = Icons.Filled.Add,
                    modifier = Modifier.weight(1f)
                )
            }

            BalanceCard(state, onCategoryClick, modifier = Modifier.padding(top = 24.dp))

            MonthOverMonthRow(state.overallMoM, modifier = Modifier.padding(top = 18.dp))
            BudgetAlertsRow(state.budgets, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))

            QuickAccessRow(
                onBudgets = onOpenBudgets,
                onRecurring = onOpenRecurring,
                onReminders = onOpenReminders,
                onGoals = onOpenGoals,
                onCategories = onOpenCategories,
                modifier = Modifier.padding(top = 20.dp)
            )

            if (FeatureFlags.SPLIT_WITH_FRIENDS_ENABLED) {
                SharedExpensesCard(
                    netMinor = state.sharedNetMinor,
                    friendCount = state.sharedFriendCount,
                    onAddExpense = onOpenGroups,
                    onSettleUp = onOpenFriends,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
            }

            if (state.topCategories.isNotEmpty()) {
                Text(
                    "Top categories",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 26.dp, bottom = 14.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.topCategories.forEachIndexed { index, item ->
                        TopCategoryRow(item, index, onClick = { onCategoryClick(item.category.id) })
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent transactions", style = MaterialTheme.typography.titleMedium)
                if (state.recentEntries.isNotEmpty()) {
                    Text(
                        "View all",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onViewAllEntries)
                    )
                }
            }
            if (state.recentEntries.isEmpty()) {
                Text(
                    "Nothing logged yet — add your first expense or income above",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.recentEntries.forEach { entry ->
                        EntryListRow(
                            entry = entry,
                            category = entry.categoryId?.let { state.categoriesById[it] },
                            onClick = { onEntryClick(entry.id) },
                            onLongClick = {},
                            paddingValues = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BalanceCard(state: DashboardUiState, onCategoryClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val negative = state.summary.netMinor < 0
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)), RoundedCornerShape(26.dp))
            .padding(22.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "NET BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnHeroGradientMuted
                    )
                    Text(
                        (if (negative) "-" else "") + state.summary.netMinor.let { if (it < 0) -it else it }.formatAsCurrency(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnHeroGradientPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                }
            }
            Text(
                if (state.summary.totalIncomeMinor == 0L) "No income logged yet this month" else "Updated for this month",
                style = MaterialTheme.typography.bodyMedium,
                color = OnHeroGradientMuted,
                modifier = Modifier.padding(top = 6.dp)
            )
            HorizontalDivider(color = OnHeroGradientMuted.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Expense", style = MaterialTheme.typography.labelMedium, color = OnHeroGradientMuted)
                    Text(
                        state.summary.totalExpenseMinor.formatAsCurrency(),
                        style = MaterialTheme.typography.titleMedium,
                        color = OnHeroGradientPrimary,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                    Text("Income", style = MaterialTheme.typography.labelMedium, color = OnHeroGradientMuted, modifier = Modifier.padding(top = 16.dp))
                    Text(
                        state.summary.totalIncomeMinor.formatAsCurrency(),
                        style = MaterialTheme.typography.titleMedium,
                        color = OnHeroGradientPrimary,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                Box(
                    modifier = Modifier.size(128.dp).padding(start = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        state.isLoading -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
                        state.breakdown.isEmpty() -> Text(
                            "No expenses\nyet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnHeroGradientMuted,
                            textAlign = TextAlign.Center
                        )
                        else -> CategoryBreakdownChart(
                            items = state.breakdown,
                            totalMinor = state.breakdownTotalMinor,
                            onSliceClick = onCategoryClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedExpensesCard(
    netMinor: Long,
    friendCount: Int,
    onAddExpense: () -> Unit,
    onSettleUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItemCard(onClick = {}, modifier = modifier, paddingValues = PaddingValues(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            GradientIconBadge(icon = Icons.Filled.PeopleAlt, gradient = BadgeGradients[1])
            Column(modifier = Modifier.weight(1f)) {
                Text("Shared expenses", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (friendCount == 0) "Nothing outstanding" else netMinorLabel(netMinor),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (friendCount == 0) MaterialTheme.colorScheme.onSurfaceVariant else netMinorColor(netMinor)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassButton(text = "Settle up", onClick = onSettleUp, modifier = Modifier.weight(1f))
            GlassButton(text = "Add expense", onClick = onAddExpense, icon = Icons.Filled.Add, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MonthOverMonthRow(change: MonthOverMonthChange?, modifier: Modifier = Modifier) {
    val pct = change?.percentageChange ?: return
    val color = if (pct > 0) ExpenseRed else IncomeGreen
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = if (pct >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
            contentDescription = null,
            tint = color,
            modifier = Modifier.padding(end = 2.dp).size(16.dp)
        )
        Text(
            text = "${if (pct >= 0) "+" else ""}${"%.0f".format(pct)}% vs last month",
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
private fun BudgetAlertsRow(budgets: List<BudgetProgress>, modifier: Modifier = Modifier) {
    val flagged = budgets.filter { it.status != BudgetStatus.SAFE }
    if (flagged.isEmpty()) return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        flagged.take(2).forEach { progress ->
            val label = progress.category?.name ?: "Overall"
            val color = if (progress.status == BudgetStatus.BREACHED) ExpenseRed else WarningAmber
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.14f), MaterialTheme.shapes.medium)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(
                    text = if (progress.status == BudgetStatus.BREACHED) "$label is over budget" else "$label is close to its budget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        }
        if (flagged.size > 2) {
            Text(
                "+${flagged.size - 2} more",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private data class QuickAccessItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val onClick: () -> Unit)

/** A horizontally scrollable row of icon shortcuts to the features that don't otherwise show
 * up anywhere on the dashboard — the same one-tap-away pattern as a real finance app's home
 * screen, instead of everything being buried three taps deep in More. */
@Composable
private fun QuickAccessRow(
    onBudgets: () -> Unit,
    onRecurring: () -> Unit,
    onReminders: () -> Unit,
    onGoals: () -> Unit,
    onCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickAccessItem("Budgets", Icons.Filled.PieChart, onBudgets),
        QuickAccessItem("Recurring", Icons.Filled.Autorenew, onRecurring),
        QuickAccessItem("Reminders", Icons.Filled.NotificationsActive, onReminders),
        QuickAccessItem("Goals", Icons.Filled.Savings, onGoals),
        QuickAccessItem("Categories", Icons.Filled.Category, onCategories)
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        itemsIndexed(items) { index, item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(72.dp).clickable(onClick = item.onClick)
            ) {
                GradientIconBadge(icon = item.icon, gradient = BadgeGradients[index % BadgeGradients.size], size = 52.dp)
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun TopCategoryRow(item: CategoryBreakdownItem, index: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val gradient = BadgeGradients[index % BadgeGradients.size]
    ListItemCard(
        onClick = onClick,
        modifier=modifier,
        paddingValues = PaddingValues(horizontal = 0.dp, vertical = 6.dp),

        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            GradientIconBadge(icon = CategoryIconProvider.iconFor(item.category.iconKey), gradient = gradient, size = 42.dp, iconSize = 20.dp)
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(item.category.name, style = MaterialTheme.typography.bodyLarge)
                    Text("${"%.0f".format(item.percentage)}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((item.percentage / 100f).coerceIn(0f, 1f))
                                .height(5.dp)
                                .background(gradient.second, RoundedCornerShape(3.dp))
                        )
                    }
                    Text(
                        item.totalMinor.formatAsCurrency(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
        }
    }
}
