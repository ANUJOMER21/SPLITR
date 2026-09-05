package com.omer.expensetracker.domain.repository.split

import com.omer.expensetracker.domain.model.split.ActivityLogEntry
import com.omer.expensetracker.domain.model.split.Balance
import com.omer.expensetracker.domain.model.split.ExpenseComment
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.FriendGroup
import com.omer.expensetracker.domain.model.split.SharedExpense
import com.omer.expensetracker.domain.model.split.Settlement
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FriendRepository {
    fun observeFriends(): Flow<List<Friend>>
    suspend fun getFriend(id: String): Friend?
    suspend fun getYou(): Friend
    suspend fun getFriendByLinkedUserId(uid: String): Friend?
    suspend fun addFriend(name: String, email: String, contactInfo: String?, avatarColorArgb: Long, linkedUserId: String?): Friend
    suspend fun updateFriend(friend: Friend)
    suspend fun deleteFriend(id: String)
    /** Stamps the signed-in account's email + uid onto the implicit "You" row, so other people
     * adding you as a friend by email can resolve straight to your real account. */
    suspend fun linkYouIdentity(email: String, uid: String)

    /** Used only by the cloud sync layer to mirror a friend discovered via a remote group/expense
     * onto this device — never called from a UI use case, and never re-enqueues to the outbox
     * (it's already the local reflection of someone else's write). */
    suspend fun upsertFromRemote(id: String, name: String, email: String, avatarColorArgb: Long, linkedUserId: String?)
}

interface GroupRepository {
    fun observeActiveGroups(): Flow<List<FriendGroup>>
    fun observeAllGroups(): Flow<List<FriendGroup>>
    suspend fun getGroup(id: String): FriendGroup?
    fun observeMemberIds(groupId: String): Flow<List<String>>
    suspend fun addGroup(name: String, iconKey: String, colorArgb: Long, memberFriendIds: List<String>): FriendGroup
    suspend fun updateGroup(group: FriendGroup)
    suspend fun setArchived(id: String, archived: Boolean)
    suspend fun addMember(groupId: String, friendId: String)
    suspend fun removeMember(groupId: String, friendId: String)

    /** Cloud-sync-only idempotent upsert by a known (remote) id — see [FriendRepository.upsertFromRemote]. */
    suspend fun upsertFromRemote(id: String, name: String, iconKey: String, colorArgb: Long, memberFriendIds: List<String>, isArchived: Boolean)
}

/** One participant's computed owed share, and (for multi-payer support) how much of the total
 * each payer actually fronted — keyed by friend id. */
data class ExpenseWrite(
    val description: String,
    val amountMinor: Long,
    val categoryId: String?,
    val splitType: com.omer.expensetracker.domain.model.split.SplitType,
    val date: LocalDate,
    val groupId: String?,
    val photoUri: String?,
    val paidByFriendId: Map<String, Long>,
    val owedByFriendId: Map<String, Long>
)

data class SharedExpenseDetail(
    val expense: SharedExpense,
    val paidByFriendId: Map<String, Long>,
    val owedByFriendId: Map<String, Long>
)

interface SharedExpenseRepository {
    fun observeAll(): Flow<List<SharedExpense>>
    fun observeForGroup(groupId: String): Flow<List<SharedExpense>>
    fun observeForFriend(friendId: String): Flow<List<SharedExpense>>
    suspend fun getDetail(id: String): SharedExpenseDetail?

    /** Adds the expense and, in the same transaction, applies its balance deltas and writes an
     * activity-log entry. [write]'s `owedByFriendId` and `paidByFriendId` must each already sum
     * exactly to the total (see [com.omer.expensetracker.domain.usecase.split.SplitCalculator]). */
    suspend fun addExpense(write: ExpenseWrite): SharedExpense

    /** Reverses the expense's prior balance impact and applies the new one — atomically, so a
     * balance can never drift even after repeated edits. */
    suspend fun editExpense(id: String, write: ExpenseWrite)

    /** Fully reverses the expense's balance impact and soft-deletes it. */
    suspend fun deleteExpense(id: String)

    fun observeComments(expenseId: String): Flow<List<ExpenseComment>>
    suspend fun addComment(expenseId: String, text: String)

    /** Cloud-sync-only idempotent upsert by a known (remote) id — see [FriendRepository.upsertFromRemote]. */
    suspend fun upsertFromRemote(id: String, write: ExpenseWrite)

    /** Cloud-sync-only: reverses and soft-deletes an expense that was deleted on another device. */
    suspend fun deleteFromRemote(id: String)
}

interface BalanceRepository {
    fun observeOverallBalance(friendId: String): Flow<Long>
    fun observeFriendBalances(friendId: String): Flow<List<Balance>>
    fun observeGroupBalances(groupId: String): Flow<List<Balance>>
    fun observeAllOverallBalances(): Flow<List<Balance>>
}

interface SettlementRepository {
    fun observeAll(): Flow<List<Settlement>>
    fun observeForFriend(friendId: String): Flow<List<Settlement>>
    suspend fun getSettlement(id: String): Settlement?
    suspend fun recordSettlement(
        payerFriendId: String,
        receiverFriendId: String,
        amountMinor: Long,
        date: LocalDate,
        note: String?,
        groupId: String?
    ): Settlement

    /** Records one friend-level settlement split across several buckets in a single transaction —
     * [allocations] maps a groupId (or `null` for the non-group bucket) to the amount that slice
     * settles. When more than one slice is written they share a generated `batchId`, so
     * [deleteSettlementBatch] can reverse the whole payment at once. Each slice applies its
     * balance delta and syncs exactly like a plain [recordSettlement]. */
    suspend fun recordAllocatedSettlement(
        payerFriendId: String,
        receiverFriendId: String,
        date: LocalDate,
        note: String?,
        allocations: Map<String?, Long>
    ): List<Settlement>

    suspend fun editSettlement(id: String, amountMinor: Long, date: LocalDate, note: String?)
    suspend fun deleteSettlement(id: String)

    /** Reverses and soft-deletes every slice of a multi-group settlement sharing [batchId]. */
    suspend fun deleteSettlementBatch(batchId: String)

    /** Cloud-sync-only idempotent upsert by a known (remote) id — see [FriendRepository.upsertFromRemote]. */
    suspend fun upsertFromRemote(id: String, payerFriendId: String, receiverFriendId: String, amountMinor: Long, date: LocalDate, note: String?, groupId: String?, batchId: String?)

    /** Cloud-sync-only: reverses and soft-deletes a settlement that was deleted on another device. */
    suspend fun deleteFromRemote(id: String)
}

interface ActivityLogRepository {
    fun observeAll(): Flow<List<ActivityLogEntry>>
    fun observeForGroup(groupId: String): Flow<List<ActivityLogEntry>>
}
