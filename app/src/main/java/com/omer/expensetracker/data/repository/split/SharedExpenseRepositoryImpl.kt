package com.omer.expensetracker.data.repository.split

import androidx.room.withTransaction
import com.omer.expensetracker.data.local.AppDatabase
import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.local.dao.split.ActivityLogDao
import com.omer.expensetracker.data.local.dao.split.BalanceDao
import com.omer.expensetracker.data.local.dao.split.CommentAttachmentDao
import com.omer.expensetracker.data.local.dao.split.SharedExpenseDao
import com.omer.expensetracker.data.local.entity.EntryEntity
import com.omer.expensetracker.data.local.entity.split.ActivityLogEntity
import com.omer.expensetracker.data.local.entity.split.BalanceEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseCommentEntity
import com.omer.expensetracker.data.local.entity.split.ExpensePayerEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseSplitEntity
import com.omer.expensetracker.data.local.entity.split.SharedExpenseEntity
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
import com.omer.expensetracker.domain.model.split.ActivityType
import com.omer.expensetracker.domain.model.split.SharedExpense
import com.omer.expensetracker.domain.model.split.SplitType
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.ExpenseWrite
import com.omer.expensetracker.domain.repository.split.SharedExpenseDetail
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * The one place shared-expense balance math happens. Every add/edit/delete runs inside a single
 * [AppDatabase.withTransaction] block that writes the expense (+ its payers/splits), applies the
 * resulting balance deltas, and appends an activity-log entry — so a balance can never drift out
 * of sync with the expenses that produced it, and an edit or delete always leaves the ledger
 * exactly as if the change (or the original action) had never happened.
 *
 * [upsertFromRemote]/[deleteFromRemote] reuse the exact same core (insert/replace/reverse) as the
 * regular UI-facing methods, so a change pulled from another device's Firestore write goes
 * through the identical ledger-integrity path — the only difference is they never re-enqueue to
 * the local outbox (that would just push the same change straight back to Firestore).
 */
class SharedExpenseRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val expenseDao: SharedExpenseDao,
    private val balanceDao: BalanceDao,
    private val activityLogDao: ActivityLogDao,
    private val commentAttachmentDao: CommentAttachmentDao,
    private val entryDao: EntryDao,
    private val syncOutbox: SyncOutbox
) : SharedExpenseRepository {

    override fun observeAll(): Flow<List<SharedExpense>> =
        expenseDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeForGroup(groupId: String): Flow<List<SharedExpense>> =
        expenseDao.observeForGroup(groupId).map { list -> list.map { it.toDomain() } }

    override fun observeForFriend(friendId: String): Flow<List<SharedExpense>> =
        expenseDao.observeForFriend(friendId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDetail(id: String): SharedExpenseDetail? {
        val entity = expenseDao.getById(id) ?: return null
        val payers = expenseDao.getPayers(id).associate { it.friendId to it.paidAmountMinor }
        val splits = expenseDao.getSplits(id).associate { it.friendId to it.owedAmountMinor }
        return SharedExpenseDetail(entity.toDomain(), payers, splits)
    }

    override suspend fun addExpense(write: ExpenseWrite): SharedExpense = db.withTransaction {
        val id = UUID.randomUUID().toString()
        val entity = insertNew(id, write)
        syncOutbox.enqueue(SyncEntityType.EXPENSE, id, SyncOperation.UPSERT)
        entity
    }

    override suspend fun editExpense(id: String, write: ExpenseWrite): Unit = db.withTransaction {
        val existing = expenseDao.getById(id) ?: return@withTransaction
        replaceExisting(existing, write)
        syncOutbox.enqueue(SyncEntityType.EXPENSE, id, SyncOperation.UPSERT)
    }

    override suspend fun deleteExpense(id: String): Unit = db.withTransaction {
        val existing = expenseDao.getById(id) ?: return@withTransaction
        reverseAndSoftDelete(existing)
        syncOutbox.enqueue(SyncEntityType.EXPENSE, id, SyncOperation.DELETE)
    }

    override suspend fun upsertFromRemote(id: String, write: ExpenseWrite): Unit = db.withTransaction {
        val existing = expenseDao.getById(id)
        if (existing == null) insertNew(id, write) else replaceExisting(existing, write)
    }

    override suspend fun deleteFromRemote(id: String): Unit = db.withTransaction {
        val existing = expenseDao.getById(id) ?: return@withTransaction
        reverseAndSoftDelete(existing)
    }

    override fun observeComments(expenseId: String) =
        commentAttachmentDao.observeComments(expenseId).map { list -> list.map { it.toDomain() } }

    override suspend fun addComment(expenseId: String, text: String) {
        commentAttachmentDao.insertComment(
            ExpenseCommentEntity(id = UUID.randomUUID().toString(), expenseId = expenseId, text = text, createdAt = System.currentTimeMillis())
        )
    }

    private suspend fun insertNew(id: String, write: ExpenseWrite): SharedExpense {
        val now = System.currentTimeMillis()
        val entity = write.toEntity(id, now, now)
        expenseDao.insertExpense(entity)
        writePayersAndSplits(id, write)
        applyDeltas(deltasOf(write), write.groupId)
        logActivity(write.groupId, ActivityType.EXPENSE_ADDED, "${write.description} added — ${formatMinor(write.amountMinor)}", now)
        syncLinkedEntry(id, write)
        return entity.toDomain()
    }

    private suspend fun replaceExisting(existing: SharedExpenseEntity, write: ExpenseWrite) {
        val id = existing.id
        val oldWrite = existing.toWrite(
            paidBy = expenseDao.getPayers(id).associate { it.friendId to it.paidAmountMinor },
            owedBy = expenseDao.getSplits(id).associate { it.friendId to it.owedAmountMinor }
        )
        // Reverse the prior impact, then delete and re-insert the payer/split rows.
        applyDeltas(deltasOf(oldWrite).mapValues { -it.value }, existing.groupId)
        expenseDao.deletePayers(id)
        expenseDao.deleteSplits(id)

        val now = System.currentTimeMillis()
        expenseDao.updateExpense(write.toEntity(id, existing.createdAt, now))
        writePayersAndSplits(id, write)
        applyDeltas(deltasOf(write), write.groupId)
        logActivity(write.groupId, ActivityType.EXPENSE_EDITED, "${write.description} edited", now)
        syncLinkedEntry(id, write)
    }

    private suspend fun reverseAndSoftDelete(existing: SharedExpenseEntity) {
        val id = existing.id
        val write = existing.toWrite(
            paidBy = expenseDao.getPayers(id).associate { it.friendId to it.paidAmountMinor },
            owedBy = expenseDao.getSplits(id).associate { it.friendId to it.owedAmountMinor }
        )
        applyDeltas(deltasOf(write).mapValues { -it.value }, existing.groupId)
        val now = System.currentTimeMillis()
        expenseDao.softDeleteExpense(id, now)
        logActivity(existing.groupId, ActivityType.EXPENSE_DELETED, "${existing.description} deleted", now)
        entryDao.getByLinkedSharedExpenseId(id)?.let { entryDao.softDelete(it.id, now) }
    }

    /** Mirrors your own share of the expense (if any) into a personal entry, so it counts
     * toward your regular Entries list and dashboard totals exactly like money you spent any
     * other way — see [com.omer.expensetracker.data.local.entity.EntryEntity.linkedSharedExpenseId]. */
    private suspend fun syncLinkedEntry(expenseId: String, write: ExpenseWrite) {
        val yourShare = write.owedByFriendId[YOU_FRIEND_ID] ?: 0L
        val existing = entryDao.getByLinkedSharedExpenseId(expenseId)
        val now = System.currentTimeMillis()
        when {
            yourShare <= 0L -> existing?.let { entryDao.softDelete(it.id, now) }
            existing != null -> entryDao.update(
                existing.copy(amountMinor = yourShare, categoryId = write.categoryId, dateEpochDay = write.date.toEpochDay(), updatedAt = now)
            )
            else -> entryDao.insert(
                EntryEntity(
                    id = UUID.randomUUID().toString(), type = "EXPENSE", amountMinor = yourShare,
                    categoryId = write.categoryId, dateEpochDay = write.date.toEpochDay(),
                    createdAt = now, updatedAt = now, linkedSharedExpenseId = expenseId
                )
            )
        }
    }

    private suspend fun writePayersAndSplits(expenseId: String, write: ExpenseWrite) {
        expenseDao.insertPayers(write.paidByFriendId.map { (friendId, amount) -> ExpensePayerEntity(UUID.randomUUID().toString(), expenseId, friendId, amount) })
        expenseDao.insertSplits(write.owedByFriendId.map { (friendId, amount) -> ExpenseSplitEntity(UUID.randomUUID().toString(), expenseId, friendId, amount) })
    }

    /** Each non-owner friend's balance moves by (what they owe − what they actually paid):
     * owing more without paying pushes their balance up (they owe You more); paying more than
     * their share pulls it down (You owe them). "You" never carries a balance against yourself. */
    private fun deltasOf(write: ExpenseWrite): Map<String, Long> {
        val deltas = LinkedHashMap<String, Long>()
        (write.owedByFriendId.keys + write.paidByFriendId.keys).forEach { friendId ->
            if (friendId == YOU_FRIEND_ID) return@forEach
            val owed = write.owedByFriendId[friendId] ?: 0L
            val paid = write.paidByFriendId[friendId] ?: 0L
            deltas[friendId] = owed - paid
        }
        return deltas
    }

    private suspend fun applyDeltas(deltas: Map<String, Long>, groupId: String?) {
        deltas.forEach { (friendId, delta) ->
            if (delta == 0L) return@forEach
            adjustBalance(friendId, null, delta)
            if (groupId != null) adjustBalance(friendId, groupId, delta)
        }
    }

    private suspend fun adjustBalance(friendId: String, groupId: String?, delta: Long) {
        val id = BalanceEntity.makeId(friendId, groupId)
        val existing = balanceDao.getById(id)
        balanceDao.upsert(BalanceEntity(id = id, friendId = friendId, groupId = groupId, netMinor = (existing?.netMinor ?: 0L) + delta))
    }

    private suspend fun logActivity(groupId: String?, type: ActivityType, summary: String, timestamp: Long) {
        activityLogDao.insert(ActivityLogEntity(id = UUID.randomUUID().toString(), groupId = groupId, type = type.name, summary = summary, timestamp = timestamp))
    }
}

private fun ExpenseWrite.toEntity(id: String, createdAt: Long, updatedAt: Long) = SharedExpenseEntity(
    id = id, description = description, amountMinor = amountMinor, categoryId = categoryId,
    splitType = splitType.name, dateEpochDay = date.toEpochDay(), groupId = groupId, photoUri = photoUri,
    createdAt = createdAt, updatedAt = updatedAt
)

private fun SharedExpenseEntity.toWrite(paidBy: Map<String, Long>, owedBy: Map<String, Long>) = ExpenseWrite(
    description = description, amountMinor = amountMinor, categoryId = categoryId,
    splitType = SplitType.valueOf(splitType), date = LocalDate.ofEpochDay(dateEpochDay), groupId = groupId,
    photoUri = photoUri, paidByFriendId = paidBy, owedByFriendId = owedBy
)

private fun formatMinor(amountMinor: Long): String = "%.2f".format(amountMinor / 100.0)
