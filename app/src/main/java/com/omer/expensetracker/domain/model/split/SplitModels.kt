package com.omer.expensetracker.domain.model.split

import java.time.LocalDate

/** A person the app owner tracks shared expenses with. The owner themself is always present
 * as the implicit "You" record ([isYou]), so personal and shared participation share one
 * underlying model — "you owe Bob" and "Bob owes you" are the same structure from two sides. */
data class Friend(
    val id: String,
    val name: String,
    val email: String,
    val contactInfo: String?,
    val avatarColorArgb: Long,
    val isYou: Boolean,
    /** The friend's real account uid, once their email resolves to a registered Expense
     * Tracker user — this is what makes shared expenses actually sync to their device. Null
     * means they're tracked locally only, until they sign up with this same email. */
    val linkedUserId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class FriendGroup(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class GroupMember(
    val groupId: String,
    val friendId: String,
    val joinedAt: Long
)

enum class SplitType { EQUAL, EXACT, PERCENTAGE, SHARES, ITEMIZED }

data class SharedExpense(
    val id: String,
    val description: String,
    val amountMinor: Long,
    val categoryId: String?,
    val splitType: SplitType,
    val date: LocalDate,
    val groupId: String?,
    val photoUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

/** How much of [expenseId] each payer actually fronted — multiple payers on one expense are
 * supported, so a balance never assumes a single "paid by" person. */
data class ExpensePayer(
    val id: String,
    val expenseId: String,
    val friendId: String,
    val paidAmountMinor: Long
)

/** How much of [expenseId] each participant owes, computed by [com.omer.expensetracker.domain.usecase.split.SplitCalculator]. */
data class ExpenseSplit(
    val id: String,
    val expenseId: String,
    val friendId: String,
    val owedAmountMinor: Long
)

data class ExpenseItem(
    val id: String,
    val expenseId: String,
    val name: String,
    val amountMinor: Long
)

data class ExpenseItemShare(
    val itemId: String,
    val friendId: String
)

data class ExpenseComment(
    val id: String,
    val expenseId: String,
    val text: String,
    val createdAt: Long
)

data class ExpenseAttachment(
    val id: String,
    val expenseId: String,
    val uri: String,
    val createdAt: Long
)

/** A materialized, always-up-to-date net balance between [friendId] and the app owner —
 * positive means the friend owes the owner, negative means the owner owes the friend.
 * [groupId] is null for the friend's overall (all-groups + direct) balance and non-null for
 * their balance scoped to one specific group. */
data class Balance(
    val friendId: String,
    val groupId: String?,
    val netMinor: Long
)

data class Settlement(
    val id: String,
    val payerFriendId: String,
    val receiverFriendId: String,
    val amountMinor: Long,
    val date: LocalDate,
    val note: String?,
    val groupId: String?,
    /** Shared by every slice of one friend-level settlement that was split across multiple
     * groups; null for a plain single-bucket settlement. See [ExpenseSplit] neighbours. */
    val batchId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

enum class ActivityType {
    EXPENSE_ADDED, EXPENSE_EDITED, EXPENSE_DELETED,
    SETTLEMENT_RECORDED, SETTLEMENT_EDITED, SETTLEMENT_DELETED,
    MEMBER_ADDED, MEMBER_REMOVED, GROUP_RENAMED, GROUP_CREATED
}

data class ActivityLogEntry(
    val id: String,
    val groupId: String?,
    val type: ActivityType,
    val summary: String,
    val timestamp: Long
)

/** A friend's full picture: net balance plus the expenses/settlements that produced it. */
data class FriendDetail(
    val friend: Friend,
    val netMinor: Long,
    val activity: List<ActivityLogEntry>
)

data class GroupDetail(
    val group: FriendGroup,
    val members: List<Friend>,
    val memberBalances: Map<String, Long>,
    val expenses: List<SharedExpense>,
    val activity: List<ActivityLogEntry>
)
