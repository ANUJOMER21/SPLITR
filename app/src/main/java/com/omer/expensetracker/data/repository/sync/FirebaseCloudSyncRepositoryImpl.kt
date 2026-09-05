package com.omer.expensetracker.data.repository.sync

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.data.local.dao.sync.SyncQueueDao
import com.omer.expensetracker.data.local.entity.sync.SyncQueueEntity
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.model.sync.SyncState
import com.omer.expensetracker.domain.model.sync.SyncStatus
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.split.GroupRepository
import com.omer.expensetracker.domain.repository.split.SettlementRepository
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import com.omer.expensetracker.domain.repository.sync.AuthRepository
import com.omer.expensetracker.domain.repository.sync.CloudSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pushes local changes up to Firestore, guarded end-to-end by [FeatureFlags.CLOUD_SYNC_ENABLED]:
 * every mutation already lands in [SyncQueueDao] via [SyncOutbox] regardless of the flag (cheap
 * local bookkeeping), but draining that queue — the only place a real network call happens —
 * only runs when the flag is on and a user is signed in.
 *
 * Friends are a private per-account backup (`users/{uid}/friends/{id}`) — that's what makes your
 * own friend list follow you across your own devices. Groups, expenses and settlements are
 * shared top-level documents keyed by a `memberUids` array, so a friend who has linked their own
 * account (see [Friend.linkedUserId]) receives them too — that's the actual cross-account sync.
 * Pulling those shared documents back down happens in [FirestoreSyncCoordinator], not here.
 */
@Singleton
class FirebaseCloudSyncRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncQueueDao: SyncQueueDao,
    private val friendRepository: FriendRepository,
    private val groupRepository: GroupRepository,
    private val sharedExpenseRepository: SharedExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val deviceIdProvider: DeviceIdProvider
) : CloudSyncRepository {

    private val firestore by lazy { Firebase.firestore }

    override fun observeSyncState(): Flow<SyncState> =
        combine(authRepository.observeCurrentUser(), syncQueueDao.observePendingCount()) { user, pending ->
            SyncState(
                status = if (user != null) SyncStatus.SIGNED_IN else SyncStatus.SIGNED_OUT,
                user = user,
                pendingUploads = pending
            )
        }

    override suspend fun claimLocalData() {
        friendRepository.observeFriends().first().forEach { syncQueueDao.enqueue(outboxRow(SyncEntityType.FRIEND, it.id)) }
        groupRepository.observeAllGroups().first().forEach { syncQueueDao.enqueue(outboxRow(SyncEntityType.GROUP, it.id)) }
        sharedExpenseRepository.observeAll().first().forEach { syncQueueDao.enqueue(outboxRow(SyncEntityType.EXPENSE, it.id)) }
        settlementRepository.observeAll().first().forEach { syncQueueDao.enqueue(outboxRow(SyncEntityType.SETTLEMENT, it.id)) }
    }

    override suspend fun syncNow() {
        if (!FeatureFlags.CLOUD_SYNC_ENABLED) return
        val uid = Firebase.auth.currentUser?.uid ?: return

        syncQueueDao.pending().forEach { entry ->
            runCatching { push(uid, entry) }.onSuccess { syncQueueDao.remove(entry) }
            // On failure the row stays queued — it's retried on the next syncNow() call.
        }
    }

    private suspend fun push(uid: String, entry: SyncQueueEntity) {
        when (entry.entityType) {
            SyncEntityType.FRIEND -> {
                val doc = firestore.collection("users").document(uid).collection("friends").document(entry.entityId)
                if (entry.operation == SyncOperation.DELETE) { doc.delete().await(); return }
                val friend = friendRepository.getFriend(entry.entityId) ?: return
                doc.set(
                    mapOf(
                        "name" to friend.name, "email" to friend.email, "contactInfo" to friend.contactInfo,
                        "avatarColorArgb" to friend.avatarColorArgb, "isYou" to friend.isYou, "linkedUserId" to friend.linkedUserId,
                        "createdAt" to friend.createdAt, "updatedAt" to friend.updatedAt
                    )
                ).await()
            }
            SyncEntityType.GROUP -> {
                val doc = firestore.collection("groups").document(entry.entityId)
                if (entry.operation == SyncOperation.DELETE) { doc.delete().await(); return }
                val group = groupRepository.getGroup(entry.entityId) ?: return
                val memberIds = groupRepository.observeMemberIds(group.id).first()
                val (memberUids, memberMeta) = resolveMembers(uid, memberIds)
                doc.set(
                    mapOf(
                        "name" to group.name, "iconKey" to group.iconKey, "colorArgb" to group.colorArgb,
                        "isArchived" to group.isArchived, "memberUids" to memberUids, "memberMeta" to memberMeta,
                        "lastEditedByDeviceId" to deviceIdProvider.deviceId, "lastEditedByUid" to uid,
                        "createdAt" to group.createdAt, "updatedAt" to group.updatedAt
                    )
                ).await()
            }
            SyncEntityType.EXPENSE -> {
                val doc = firestore.collection("sharedExpenses").document(entry.entityId)
                if (entry.operation == SyncOperation.DELETE) { doc.delete().await(); return }
                val detail = sharedExpenseRepository.getDetail(entry.entityId) ?: return
                val participantIds = (detail.paidByFriendId.keys + detail.owedByFriendId.keys)
                val (memberUids, memberMeta) = resolveMembers(uid, participantIds)
                doc.set(
                    mapOf(
                        "description" to detail.expense.description, "amountMinor" to detail.expense.amountMinor,
                        "categoryId" to detail.expense.categoryId, "splitType" to detail.expense.splitType.name,
                        "dateEpochDay" to detail.expense.date.toEpochDay(), "groupId" to detail.expense.groupId,
                        "paidBy" to detail.paidByFriendId.mapKeys { participantKey(uid, it.key) },
                        "owedBy" to detail.owedByFriendId.mapKeys { participantKey(uid, it.key) },
                        "memberUids" to memberUids, "memberMeta" to memberMeta,
                        "lastEditedByDeviceId" to deviceIdProvider.deviceId, "lastEditedByUid" to uid,
                        "createdAt" to detail.expense.createdAt, "updatedAt" to detail.expense.updatedAt
                    )
                ).await()
            }
            SyncEntityType.SETTLEMENT -> {
                val doc = firestore.collection("settlements").document(entry.entityId)
                if (entry.operation == SyncOperation.DELETE) { doc.delete().await(); return }
                val settlement = settlementRepository.getSettlement(entry.entityId) ?: return
                val (memberUids, memberMeta) = resolveMembers(uid, listOf(settlement.payerFriendId, settlement.receiverFriendId))
                doc.set(
                    mapOf(
                        "payerKey" to participantKey(uid, settlement.payerFriendId),
                        "receiverKey" to participantKey(uid, settlement.receiverFriendId),
                        "amountMinor" to settlement.amountMinor, "dateEpochDay" to settlement.date.toEpochDay(),
                        "note" to settlement.note, "groupId" to settlement.groupId,
                        "memberUids" to memberUids, "memberMeta" to memberMeta,
                        "lastEditedByDeviceId" to deviceIdProvider.deviceId, "lastEditedByUid" to uid,
                        "createdAt" to settlement.createdAt, "updatedAt" to settlement.updatedAt
                    )
                ).await()
            }
        }
    }

    /** A local friend id becomes their real uid once linked, or a per-owner synthetic key
     * ("local:<myUid>:<friendId>") while they're still local-only — see [Friend.linkedUserId]. */
    private suspend fun participantKey(myUid: String, localFriendId: String): String {
        if (localFriendId == YOU_FRIEND_ID) return myUid
        val friend = friendRepository.getFriend(localFriendId)
        return friend?.linkedUserId ?: "local:$myUid:$localFriendId"
    }

    private suspend fun resolveMembers(myUid: String, localFriendIds: Collection<String>): Pair<List<String>, Map<String, Map<String, Any?>>> {
        val memberUids = mutableSetOf(myUid)
        val memberMeta = mutableMapOf<String, Map<String, Any?>>()
        memberMeta[myUid] = mapOf("name" to "You")
        localFriendIds.distinct().forEach { localId ->
            if (localId == YOU_FRIEND_ID) return@forEach
            val friend = friendRepository.getFriend(localId) ?: return@forEach
            val key = friend.linkedUserId ?: "local:$myUid:$localId"
            if (friend.linkedUserId != null) memberUids += friend.linkedUserId
            memberMeta[key] = mapOf("name" to friend.name, "avatarColorArgb" to friend.avatarColorArgb)
        }
        return memberUids.toList() to memberMeta
    }

    private fun outboxRow(entityType: String, entityId: String) = SyncQueueEntity(
        id = "$entityType:$entityId", entityType = entityType, entityId = entityId,
        operation = SyncOperation.UPSERT, payloadJson = "", createdAt = System.currentTimeMillis()
    )
}
