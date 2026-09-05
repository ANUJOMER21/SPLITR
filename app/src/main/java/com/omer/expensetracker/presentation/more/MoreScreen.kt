package com.omer.expensetracker.presentation.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.domain.model.sync.SyncStatus
import com.omer.expensetracker.presentation.account.AccountViewModel
import com.omer.expensetracker.presentation.components.GradientIconBadge
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.ui.theme.BadgeGradients
import com.omer.expensetracker.ui.theme.HeroGradientEnd
import com.omer.expensetracker.ui.theme.HeroGradientStart
import com.omer.expensetracker.ui.theme.OnHeroGradientMuted
import com.omer.expensetracker.ui.theme.OnHeroGradientPrimary

private data class MoreItem(val label: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

private sealed interface MoreRow {
    data class SectionHeader(val title: String) : MoreRow
    data class Entry(val item: MoreItem, val badgeIndex: Int) : MoreRow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onBudgets: () -> Unit,
    onRecurring: () -> Unit,
    onReminders: () -> Unit,
    onGoals: () -> Unit,
    onBackup: () -> Unit,
    onCategories: () -> Unit,
    onFriends: () -> Unit = {},
    onGroups: () -> Unit = {},
    onAccount: () -> Unit = {},
    onSettings: () -> Unit = {},
    viewModel: MoreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val seedResult by viewModel.seedResult.collectAsState()
    LaunchedEffect(seedResult) {
        seedResult?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.consumeSeedResult()
        }
    }

    var badgeCounter = 0
    val rows = buildList {
        add(MoreRow.SectionHeader("Manage"))
        add(MoreRow.Entry(MoreItem("Categories", "Add, edit, or hide spending categories", Icons.Filled.Category, onCategories), badgeCounter++))
        add(MoreRow.Entry(MoreItem("Budgets", "Set monthly limits per category", Icons.Filled.PieChart, onBudgets), badgeCounter++))
        add(MoreRow.Entry(MoreItem("Recurring", "Bills and income that repeat automatically", Icons.Filled.Autorenew, onRecurring), badgeCounter++))
        add(MoreRow.Entry(MoreItem("Bill reminders", "Never miss a due date", Icons.Filled.NotificationsActive, onReminders), badgeCounter++))
        add(MoreRow.Entry(MoreItem("Savings goals", "Track progress toward a target", Icons.Filled.Savings, onGoals), badgeCounter++))

        if (FeatureFlags.SPLIT_WITH_FRIENDS_ENABLED) {
            add(MoreRow.SectionHeader("Split with friends"))
            add(MoreRow.Entry(MoreItem("Friends", "Track shared expenses and balances", Icons.Filled.PeopleAlt, onFriends), badgeCounter++))
            add(MoreRow.Entry(MoreItem("Groups", "Split expenses with a trip, household, or team", Icons.Filled.Groups, onGroups), badgeCounter++))
            if (FeatureFlags.SPLIT_SAMPLE_DATA_SEED) {
                add(MoreRow.Entry(MoreItem("Load sample split data", "Seed friends, groups and shared expenses for testing", Icons.Filled.Autorenew) { viewModel.seedSampleSplitData() }, badgeCounter++))
            }
        }

        add(MoreRow.SectionHeader("Data"))
        add(MoreRow.Entry(MoreItem("Backup & restore", "Export or import all your data", Icons.Filled.CloudSync, onBackup), badgeCounter++))
        add(MoreRow.Entry(MoreItem("Settings", "Onboarding, reset app data", Icons.Filled.Tune, onSettings), badgeCounter++))
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("More", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (FeatureFlags.CLOUD_SYNC_ENABLED) {
                item { AccountHeaderCard(onClick = onAccount) }
            }

            items(rows) { row ->
                when (row) {
                    is MoreRow.SectionHeader -> Text(
                        row.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp, start = 4.dp)
                    )
                    is MoreRow.Entry -> ListItemCard(
                        onClick = row.item.onClick,
                        modifier = Modifier.padding(vertical = 4.dp).animateItem(),
                        paddingValues = PaddingValues(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            GradientIconBadge(icon = row.item.icon, gradient = BadgeGradients[row.badgeIndex % BadgeGradients.size])
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.item.label, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    row.item.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountHeaderCard(onClick: () -> Unit, viewModel: AccountViewModel = hiltViewModel()) {
    val state by viewModel.syncState.collectAsState()
    val signedIn = state.status != SyncStatus.SIGNED_OUT

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = OnHeroGradientPrimary)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                Text(
                    if (signedIn) (state.user?.displayName ?: state.user?.email ?: "Signed in") else "Sign in",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnHeroGradientPrimary
                )
                Text(
                    if (signedIn) "Synced across your devices" else "Link your account to sync friends and groups",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnHeroGradientMuted
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = OnHeroGradientMuted)
        }
    }
}
