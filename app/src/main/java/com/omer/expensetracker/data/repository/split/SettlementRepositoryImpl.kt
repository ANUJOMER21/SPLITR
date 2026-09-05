package com.omer.expensetracker.data.repository.split

import androidx.room.withTransaction
import com.omer.expensetracker.data.local.AppDatabase
import com.omer.expensetracker.data.local.dao.split.ActivityLogDao
import com.omer.expensetracker.data.local.dao.split.BalanceDao
import com.omer.expensetracker.data.local.dao.split.SettlementDao
import com.omer.expensetracker.data.local.entity.split.ActivityLogEntity
import com.omer.expensetracker.data.local.entity.split.BalanceEntity
import com.omer.expensetracker.data.local.entity.split.SettlementEntity
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
import com.omer.expensetracker.domain.model.split.ActivityType
import com.omer.expensetracker.domain.model.split.Settlement
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.SettlementRepository
import com.omer.expensetracker.domain.service.WidgetRefresher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/** Recording a settlement moves money exactly like a shared-expense delta would, reversed the
 * same transactional way on edit/delete — see [SharedExpenseRepositoryImpl] for the underlying
 * ledger-integrity rule. */
class SettlementRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val settlementDao: SettlementDao,
    private val balanceDao: BalanceDao,
    private val activityLogDao: ActivityLogDao,
    private val syncOutbox: SyncOutbox,
    private val widgetRefresher: WidgetRefresher
) : SettlementRepository {

    override fun observeAll(): Flow<List<Settlement>> =
        settlementDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeForFriend(friendId: String): Flow<List<Settlement>> =
        settlementDao.observeForFriend(friendId).map { list -> list.map { it.toDomain() } }

    override suspend fun getSettlement(id: String): Settlement? = settlementDao.getById(id)?.toDomain()

    override suspend fun recordSettlement(
        payerFriendId: String,
        receiverFriendId: String,
        amountMinor: Long,
        date: LocalDate,
        note: String?,
        groupId: String?
    ): Settlement {
        val entity = db.withTransaction {
            val now = System.currentTimeMillis()
            val entity = SettlementEntity(
                id = UUID.randomUUID().toString(), payerFriendId = payerFriendId, receiverFriendId = receiverFriendId,
                amountMinor = amountMinor, dateEpochDay = date.toEpochDay(), note = note, groupId = groupId,
                createdAt = now, updatedAt = now
            )
            settlementDao.insert(entity)
            applyDelta(payerFriendId, receiverFriendId, amountMinor, groupId)
            logActivity(groupId, ActivityType.SETTLEMENT_RECORDED, "Settlement recorded — ${"%.2f".format(amountMinor / 100.0)}", now)
            syncOutbox.enqueue(SyncEntityType.SETTLEMENT, entity.id, SyncOperation.UPSERT)
            entity
        }
        widgetRefresher.refreshAll()
        return entity.toDomain()
    }

    override suspend fun editSettlement(id: String, amountMinor: Long, date: LocalDate, note: String?) {
        db.withTransaction {
            val existing = settlementDao.getById(id) ?: return@withTransaction
            applyDelta(existing.payerFriendId, existing.receiverFriendId, -existing.amountMinor, existing.groupId)
            val now = System.currentTimeMillis()
            settlementDao.update(existing.copy(amountMinor = amountMinor, dateEpochDay = date.toEpochDay(), note = note, updatedAt = now))
            applyDelta(existing.payerFriendId, existing.receiverFriendId, amountMinor, existing.groupId)
            logActivity(existing.groupId, ActivityType.SETTLEMENT_EDITED, "Settlement updated", now)
            syncOutbox.enqueue(SyncEntityType.SETTLEMENT, id, SyncOperation.UPSERT)
        }
        widgetRefresher.refreshAll()
    }

    override suspend fun deleteSettlement(id: String) {
        db.withTransaction {
            val existing = settlementDao.getById(id) ?: return@withTransaction
            applyDelta(existing.payerFriendId, existing.receiverFriendId, -existing.amountMinor, existing.groupId)
            val now = System.currentTimeMillis()
            settlementDao.softDelete(id, now)
            logActivity(existing.groupId, ActivityType.SETTLEMENT_DELETED, "Settlement deleted", now)
            syncOutbox.enqueue(SyncEntityType.SETTLEMENT, id, SyncOperation.DELETE)
        }
        widgetRefresher.refreshAll()
    }

    override suspend fun upsertFromRemote(id: String, payerFriendId: String, receiverFriendId: String, amountMinor: Long, date: LocalDate, note: String?, groupId: String?) {
        db.withTransaction {
            val existing = settlementDao.getById(id)
            val now = System.currentTimeMillis()
            if (existing == null) {
                settlementDao.insert(
                    SettlementEntity(
                        id = id, payerFriendId = payerFriendId, receiverFriendId = receiverFriendId,
                        amountMinor = amountMinor, dateEpochDay = date.toEpochDay(), note = note, groupId = groupId,
                        createdAt = now, updatedAt = now
                    )
                )
                applyDelta(payerFriendId, receiverFriendId, amountMinor, groupId)
            } else {
                applyDelta(existing.payerFriendId, existing.receiverFriendId, -existing.amountMinor, existing.groupId)
                settlementDao.update(
                    existing.copy(
                        payerFriendId = payerFriendId, receiverFriendId = receiverFriendId,
                        amountMinor = amountMinor, dateEpochDay = date.toEpochDay(), note = note, groupId = groupId, updatedAt = now
                    )
                )
                applyDelta(payerFriendId, receiverFriendId, amountMinor, groupId)
            }
        }
        widgetRefresher.refreshAll()
    }

    override suspend fun deleteFromRemote(id: String) {
        db.withTransaction {
            val existing = settlementDao.getById(id) ?: return@withTransaction
            applyDelta(existing.payerFriendId, existing.receiverFriendId, -existing.amountMinor, existing.groupId)
            settlementDao.softDelete(id, System.currentTimeMillis())
        }
        widgetRefresher.refreshAll()
    }

    /** Positive [amountMinor] applies the settlement; pass it negated to reverse one. Only
     * moves a balance when "You" is one side of the settlement — see [YOU_FRIEND_ID]. */
    private suspend fun applyDelta(payerFriendId: String, receiverFriendId: String, amountMinor: Long, groupId: String?) {
        val (friendId, delta) = when (YOU_FRIEND_ID) {
            payerFriendId -> receiverFriendId to amountMinor
            receiverFriendId -> payerFriendId to -amountMinor
            else -> return
        }
        if (delta == 0L) return
        adjustBalance(friendId, null, delta)
        if (groupId != null) adjustBalance(friendId, groupId, delta)
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
