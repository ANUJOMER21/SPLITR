package com.omer.expensetracker.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.omer.expensetracker.presentation.components.AuroraBackground
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.presentation.account.AccountScreen
import com.omer.expensetracker.presentation.addedit.AddEditEntryScreen
import com.omer.expensetracker.presentation.auth.SignInScreen
import com.omer.expensetracker.presentation.backup.BackupScreen
import com.omer.expensetracker.presentation.budget.AddEditBudgetScreen
import com.omer.expensetracker.presentation.budget.BudgetsScreen
import com.omer.expensetracker.presentation.categories.AddEditCategoryScreen
import com.omer.expensetracker.presentation.categories.ManageCategoriesScreen
import com.omer.expensetracker.presentation.dashboard.DashboardScreen
import com.omer.expensetracker.presentation.entrylist.EntryListScreen
import com.omer.expensetracker.presentation.goal.AddEditGoalScreen
import com.omer.expensetracker.presentation.goal.GoalDetailScreen
import com.omer.expensetracker.presentation.goal.GoalsScreen
import com.omer.expensetracker.presentation.insights.InsightsScreen
import com.omer.expensetracker.presentation.more.MoreScreen
import com.omer.expensetracker.presentation.notifications.NotificationsScreen
import com.omer.expensetracker.presentation.recurring.AddEditRecurringRuleScreen
import com.omer.expensetracker.presentation.recurring.RecurringRulesScreen
import com.omer.expensetracker.presentation.reminder.AddEditBillReminderScreen
import com.omer.expensetracker.presentation.reminder.BillRemindersScreen
import com.omer.expensetracker.presentation.split.activity.ActivityFeedScreen
import com.omer.expensetracker.presentation.split.expense.AddSharedExpenseScreen
import com.omer.expensetracker.presentation.split.expense.SettleUpScreen
import com.omer.expensetracker.presentation.split.expense.SimplifyDebtsScreen
import com.omer.expensetracker.presentation.split.friend.AddEditFriendScreen
import com.omer.expensetracker.presentation.split.friend.FriendDetailScreen
import com.omer.expensetracker.presentation.split.friend.FriendsScreen
import com.omer.expensetracker.presentation.split.group.AddEditGroupScreen
import com.omer.expensetracker.presentation.split.group.GroupDetailScreen
import com.omer.expensetracker.presentation.split.group.GroupsScreen
import com.omer.expensetracker.presentation.splash.SplashScreen
import com.omer.expensetracker.presentation.auth.AuthGateViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

private const val TRANSITION_DURATION_MS = 260

@Composable
fun ExpenseTrackerNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination.isTopLevelDestination()

    // Global sign-out guard: once past the splash gate, signing out anywhere in the app
    // (e.g. the Account screen) drops straight back to the mandatory sign-in screen rather
    // than leaving a signed-out user stranded on whatever screen they were on.
    val authGateViewModel: AuthGateViewModel = hiltViewModel()
    val isSignedIn by authGateViewModel.isSignedIn.collectAsState()
    LaunchedEffect(isSignedIn, backStackEntry) {
        val currentRoute = backStackEntry?.destination?.route
        if (isSignedIn == false && currentRoute != Screen.SignIn.route && currentRoute != Screen.Splash.route) {
            navController.navigate(Screen.SignIn.route) { popUpTo(0) { inclusive = true } }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    AuroraBackground()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            if (showBottomBar) {
                ExpenseTrackerBottomNavBar(currentDestination = backStackEntry?.destination, navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            enterTransition = {
                slideInHorizontally(tween(TRANSITION_DURATION_MS)) { it / 5 } + fadeIn(tween(TRANSITION_DURATION_MS))
            },
            exitTransition = {
                slideOutHorizontally(tween(TRANSITION_DURATION_MS)) { -it / 5 } + fadeOut(tween(TRANSITION_DURATION_MS))
            },
            popEnterTransition = {
                slideInHorizontally(tween(TRANSITION_DURATION_MS)) { -it / 5 } + fadeIn(tween(TRANSITION_DURATION_MS))
            },
            popExitTransition = {
                slideOutHorizontally(tween(TRANSITION_DURATION_MS)) { it / 5 } + fadeOut(tween(TRANSITION_DURATION_MS))
            }
        ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSignedIn = { navController.navigate(Screen.Dashboard.route) { popUpTo(0) { inclusive = true } } },
                onSignedOut = { navController.navigate(Screen.SignIn.route) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onAddExpense = { navController.navigate(Screen.AddEditEntry.route(EntryType.EXPENSE.name)) },
                onAddIncome = { navController.navigate(Screen.AddEditEntry.route(EntryType.INCOME.name)) },
                onCategoryClick = { categoryId -> navController.navigate(Screen.EntryList.route(categoryId)) },
                onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                onOpenFriends = { navController.navigate(Screen.Friends.route) },
                onOpenGroups = { navController.navigate(Screen.Groups.route) },
                onEntryClick = { entryId -> navController.navigate(Screen.AddEditEntry.route(EntryType.EXPENSE.name, entryId)) },
                onViewAllEntries = { navController.navigate(Screen.EntryList.route()) },
                onOpenBudgets = { navController.navigate(Screen.Budgets.route) },
                onOpenRecurring = { navController.navigate(Screen.RecurringRules.route) },
                onOpenReminders = { navController.navigate(Screen.BillReminders.route) },
                onOpenGoals = { navController.navigate(Screen.Goals.route) },
                onOpenCategories = { navController.navigate(Screen.ManageCategories.route) }
            )
        }

        composable(
            route = Screen.EntryList.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType; nullable = true; defaultValue = "" })
        ) {
            EntryListScreen(
                onEntryClick = { entryId ->
                    navController.navigate(Screen.AddEditEntry.route(EntryType.EXPENSE.name, entryId))
                },
                onAddEntry = { type -> navController.navigate(Screen.AddEditEntry.route(type.name)) }
            )
        }

        composable(
            route = Screen.AddEditEntry.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("entryId") { type = NavType.StringType }
            )
        ) {
            AddEditEntryScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageCategories.route) {
            ManageCategoriesScreen(
                onAddCategory = { navController.navigate(Screen.AddEditCategory.route()) },
                onEditCategory = { categoryId -> navController.navigate(Screen.AddEditCategory.route(categoryId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditCategory.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            AddEditCategoryScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.More.route) {
            MoreScreen(
                onBudgets = { navController.navigate(Screen.Budgets.route) },
                onRecurring = { navController.navigate(Screen.RecurringRules.route) },
                onReminders = { navController.navigate(Screen.BillReminders.route) },
                onGoals = { navController.navigate(Screen.Goals.route) },
                onBackup = { navController.navigate(Screen.Backup.route) },
                onCategories = { navController.navigate(Screen.ManageCategories.route) },
                onFriends = { navController.navigate(Screen.Friends.route) },
                onGroups = { navController.navigate(Screen.Groups.route) },
                onAccount = { navController.navigate(Screen.Account.route) }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        // ---- Split With Friends (Phase 3) — see FeatureFlags.SPLIT_WITH_FRIENDS_ENABLED ----

        composable(Screen.Friends.route) {
            FriendsScreen(
                onBack = { navController.popBackStack() },
                onAddFriend = { navController.navigate(Screen.AddEditFriend.route) },
                onOpenFriend = { id -> navController.navigate(Screen.FriendDetail.route(id)) }
            )
        }

        composable(Screen.AddEditFriend.route) {
            AddEditFriendScreen(onDone = { navController.popBackStack() }, onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.FriendDetail.route,
            arguments = listOf(navArgument("friendId") { type = NavType.StringType })
        ) {
            FriendDetailScreen(
                onBack = { navController.popBackStack() },
                onSettleUp = { friendId -> navController.navigate(Screen.SettleUp.route(friendId)) }
            )
        }

        composable(Screen.Groups.route) {
            GroupsScreen(
                onBack = { navController.popBackStack() },
                onAddGroup = { navController.navigate(Screen.AddEditGroup.route) },
                onOpenGroup = { id -> navController.navigate(Screen.GroupDetail.route(id)) }
            )
        }

        composable(Screen.AddEditGroup.route) {
            AddEditGroupScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onAddFriendInline = { navController.navigate(Screen.AddEditFriend.route) }
            )
        }

        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            GroupDetailScreen(
                onBack = { navController.popBackStack() },
                onAddExpense = { groupId -> navController.navigate(Screen.AddSharedExpense.route(groupId)) },
                onEditExpense = { groupId, expenseId -> navController.navigate(Screen.AddSharedExpense.route(groupId, expenseId)) },
                onSimplify = { groupId -> navController.navigate(Screen.SimplifyDebts.route(groupId)) },
                onSettleUpWithFriend = { friendId, groupId ->
                    navController.navigate(Screen.SettleUp.route(friendId = friendId, groupId = groupId))
                }
            )
        }

        composable(
            route = Screen.AddSharedExpense.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("expenseId") { type = NavType.StringType; defaultValue = NEW_ID }
            )
        ) {
            AddSharedExpenseScreen(onDone = { navController.popBackStack() }, onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.SimplifyDebts.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            SimplifyDebtsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.SettleUp.route,
            arguments = listOf(
                navArgument("friendId") { type = NavType.StringType },
                navArgument("groupId") { type = NavType.StringType; nullable = true; defaultValue = "" }
            )
        ) {
            SettleUpScreen(onDone = { navController.popBackStack() }, onBack = { navController.popBackStack() })
        }

        composable(Screen.SplitActivityFeed.route) {
            ActivityFeedScreen(onBack = { navController.popBackStack() })
        }

        // ---- Cloud sync (Phase 5) — see FeatureFlags.CLOUD_SYNC_ENABLED ----

        composable(Screen.Account.route) {
            AccountScreen(
                onSignIn = { navController.navigate(Screen.SignIn.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignedIn = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Budgets.route) {
            BudgetsScreen(
                onAddBudget = { navController.navigate(Screen.AddEditBudget.route("")) },
                onEditBudget = { categoryIdOrSentinel -> navController.navigate(Screen.AddEditBudget.route(categoryIdOrSentinel)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditBudget.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            AddEditBudgetScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RecurringRules.route) {
            RecurringRulesScreen(
                onAddRule = { navController.navigate(Screen.AddEditRecurringRule.route()) },
                onEditRule = { id -> navController.navigate(Screen.AddEditRecurringRule.route(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditRecurringRule.route,
            arguments = listOf(navArgument("ruleId") { type = NavType.StringType })
        ) {
            AddEditRecurringRuleScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BillReminders.route) {
            BillRemindersScreen(
                onAddReminder = { navController.navigate(Screen.AddEditBillReminder.route()) },
                onEditReminder = { id -> navController.navigate(Screen.AddEditBillReminder.route(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditBillReminder.route,
            arguments = listOf(navArgument("reminderId") { type = NavType.StringType })
        ) {
            AddEditBillReminderScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Goals.route) {
            GoalsScreen(
                onAddGoal = { navController.navigate(Screen.AddEditGoal.route) },
                onOpenGoal = { id -> navController.navigate(Screen.GoalDetail.route(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddEditGoal.route) {
            AddEditGoalScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GoalDetail.route,
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) {
            GoalDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Insights.route) {
            InsightsScreen()
        }

        composable(Screen.Backup.route) {
            BackupScreen(onBack = { navController.popBackStack() })
        }
    }
    }
    }
}
