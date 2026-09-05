package com.omer.expensetracker.presentation.navigation

const val NEW_ID = "new"

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")

    data object Onboarding : Screen("onboarding")

    data object Settings : Screen("settings_screen")

    data object Dashboard : Screen("dashboard")

    data object EntryList : Screen("entries?categoryId={categoryId}") {
        fun route(categoryId: String? = null) = "entries?categoryId=${categoryId ?: ""}"
    }

    data object AddEditEntry : Screen("entry/{type}/{entryId}") {
        fun route(type: String, entryId: String = NEW_ID) = "entry/$type/$entryId"
    }

    data object ManageCategories : Screen("categories")

    data object AddEditCategory : Screen("category/{categoryId}") {
        fun route(categoryId: String = NEW_ID) = "category/$categoryId"
    }

    data object More : Screen("more")

    data object Budgets : Screen("budgets")

    data object AddEditBudget : Screen("budget/{categoryId}") {
        fun route(categoryId: String) = "budget/$categoryId"
    }

    data object RecurringRules : Screen("recurring")

    data object AddEditRecurringRule : Screen("recurring/{ruleId}") {
        fun route(ruleId: String = NEW_ID) = "recurring/$ruleId"
    }

    data object BillReminders : Screen("reminders")

    data object AddEditBillReminder : Screen("reminder/{reminderId}") {
        fun route(reminderId: String = NEW_ID) = "reminder/$reminderId"
    }

    data object Goals : Screen("goals")

    data object AddEditGoal : Screen("goal_new")

    data object GoalDetail : Screen("goal/{goalId}") {
        fun route(goalId: String) = "goal/$goalId"
    }

    data object Insights : Screen("insights")

    data object Backup : Screen("backup")

    data object Notifications : Screen("notifications")

    // ---- Split With Friends (Phase 3) — see FeatureFlags.SPLIT_WITH_FRIENDS_ENABLED ----

    data object Friends : Screen("friends")

    data object AddEditFriend : Screen("friend_new")

    data object FriendDetail : Screen("friend/{friendId}") {
        fun route(friendId: String) = "friend/$friendId"
    }

    data object Groups : Screen("groups")

    data object AddEditGroup : Screen("group_new")

    data object GroupDetail : Screen("group/{groupId}") {
        fun route(groupId: String) = "group/$groupId"
    }

    data object AddSharedExpense : Screen("sharedExpense/{expenseId}?groupId={groupId}&friendId={friendId}") {
        /** [groupId] scopes the expense to a group; pass [friendId] instead (with [groupId] null)
         * for a non-group expense shared with one friend. */
        fun route(expenseId: String = NEW_ID, groupId: String? = null, friendId: String? = null) =
            "sharedExpense/$expenseId?groupId=${groupId ?: ""}&friendId=${friendId ?: ""}"
    }

    data object SimplifyDebts : Screen("group/{groupId}/simplify") {
        fun route(groupId: String) = "group/$groupId/simplify"
    }

    data object SettleUp : Screen("settle/{friendId}?groupId={groupId}") {
        fun route(friendId: String, groupId: String? = null) = "settle/$friendId?groupId=${groupId ?: ""}"
    }

    data object SplitHome : Screen("split_home")

    data object SplitActivityFeed : Screen("split_activity")

    // ---- Cloud sync (Phase 5) — see FeatureFlags.CLOUD_SYNC_ENABLED ----

    data object Account : Screen("account")

    data object SignIn : Screen("sign_in")
}
