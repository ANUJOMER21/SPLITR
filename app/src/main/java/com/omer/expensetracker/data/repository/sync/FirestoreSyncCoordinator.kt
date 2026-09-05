package com.omer.expensetracker.data.repository.sync

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.domain.model.split.SplitType
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.ExpenseWrite
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.split.GroupRepository
import com.omer.expensetracker.domain.repository.split.SettlementRepository
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import com.omer.expensetracker.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The pull half of sync: live Firestore listeners on every group/expense/settlement this
 * account is a member of, merging remote writes into local Room via each repository's
 * `upsertFromRemote` — the same transactional ledger-integrity path a local edit uses, so a
 * balance never drifts whichever device touched it last.
 *
 * Started once for the life of the signed-in session (see `ExpenseTrackerApp`) and stopped on
 * sign-out. A change this exact device just pushed is skipped via [DeviceIdProvider] — see the
 * class doc there for why a per-device id is needed instead of just the account's uid.
 */
@Singleton
class FirestoreSyncCoordinator @Inject constructor(
    private val friendRepository: FriendRepository,
    private val groupRepository: GroupRepository,
    private val sharedExpenseRepository: SharedExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val deviceIdProvider: DeviceIdProvider,
    private val notificationHelper: NotificationHelper
) {
    private val firestore by lazy { Firebase.firestore }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var registrations: List<ListenerRegistration> = emptyList()

    fun start(uid: String) {
        if (!FeatureFlags.CLOUD_SYNC_ENABLED) return
        stop()
        registrations = listOf(
            firestore.collection("users").document(uid).collection("friends")
                .addSnapshotListener { snapshot, _ -> snapshot?.documentChanges?.forEach { onFriendDoc(it.document) } },
            firestore.collection("groups").whereArrayContains("memberUids", uid)
                .addSnapshotListener { snapshot, _ -> snapshot?.documentChanges?.forEach { onGroupDoc(uid, it.document) } },
            firestore.collection("sharedExpenses").whereArrayContains("memberUids", uid)
                .addSnapshotListener { snapshot, _ -> snapshot?.documentChanges?.forEach { onExpenseDoc(uid, it.document) } },
            firestore.collection("settlements").whereArrayContains("memberUids", uid)
                .addSnapshotListener { snapshot, _ -> snapshot?.documentChanges?.forEach { onSettlementDoc(uid, it.document) } }
        )
    }

    fun stop() {
        registrations.forEach { it.remove() }
        registrations = emptyList()
    }

    private fun isOwnEcho(doc: DocumentSnapshot) = doc.getString("lastEditedByDeviceId") == deviceIdProvider.deviceId

    private fun onFriendDoc(doc: DocumentSnapshot) = scope.launch {
        if (!doc.exists()) return@launch
        friendRepository.upsertFromRemote(
            id = doc.id,
            name = doc.getString("name") ?: return@launch,
            email = doc.getString("email") ?: "",
            avatarColorArgb = doc.getLong("avatarColorArgb") ?: 0xFF3B82F6L,
            linkedUserId = doc.getString("linkedUserId")
        )
    }

    private fun onGroupDoc(myUid: String, doc: DocumentSnapshot) = scope.launch {
        if (isOwnEcho(doc)) return@launch
        if (!doc.exists()) { groupRepository.setArchived(doc.id, true); return@launch }
        @Suppress("UNCHECKED_CAST")
        val memberUids = (doc.get("memberUids") as? List<String>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val memberMeta = (doc.get("memberMeta") as? Map<String, Map<String, Any?>>).orEmpty()
        val memberFriendIds = memberUids.filter { it != myUid }.map { resolveOrCreateFriend(it, memberMeta[it]) }

        groupRepository.upsertFromRemote(
            id = doc.id,
            name = doc.getString("name") ?: return@launch,
            iconKey = doc.getString("iconKey") ?: "shopping-cart",
            colorArgb = doc.getLong("colorArgb") ?: 0xFF3B82F6L,
            memberFriendIds = memberFriendIds,
            isArchived = doc.getBoolean("isArchived") ?: false
        )
    }

    private fun onExpenseDoc(myUid: String, doc: DocumentSnapshot) = scope.launch {
        if (isOwnEcho(doc)) return@launch
        if (!doc.exists()) { sharedExpenseRepository.deleteFromRemote(doc.id); return@launch }

        @Suppress("UNCHECKED_CAST")
        val paidByRemote = (doc.get("paidBy") as? Map<String, Number>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val owedByRemote = (doc.get("owedBy") as? Map<String, Number>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val memberMeta = (doc.get("memberMeta") as? Map<String, Map<String, Any?>>).orEmpty()

        val paidBy = paidByRemote.mapNotNull { (key, amount) -> resolveParticipant(myUid, key, memberMeta)?.let { it to amount.toLong() } }.toMap()
        val owedBy = owedByRemote.mapNotNull { (key, amount) -> resolveParticipant(myUid, key, memberMeta)?.let { it to amount.toLong() } }.toMap()
        if (paidBy.isEmpty() || owedBy.isEmpty()) return@launch

        sharedExpenseRepository.upsertFromRemote(
            id = doc.id,
            write = ExpenseWrite(
                description = doc.getString("description") ?: "",
                amountMinor = doc.getLong("amountMinor") ?: 0L,
                categoryId = doc.getString("categoryId"),
                splitType = runCatching { SplitType.valueOf(doc.getString("splitType") ?: "EQUAL") }.getOrDefault(SplitType.EQUAL),
                date = LocalDate.ofEpochDay(doc.getLong("dateEpochDay") ?: 0L),
                groupId = doc.getString("groupId"),
                photoUri = null,
                paidByFriendId = paidBy,
                owedByFriendId = owedBy
            )
        )

        val authorUid = doc.getString("lastEditedByUid")
        if (authorUid != null && authorUid != myUid) {
            notificationHelper.notifySyncActivity(
                id = doc.id.hashCode(),
                title = "New shared expense",
                text = "${doc.getString("description")} — ${formatMinor(doc.getLong("amountMinor") ?: 0L)}"
            )
        }
    }

    private fun onSettlementDoc(myUid: String, doc: DocumentSnapshot) = scope.launch {
        if (isOwnEcho(doc)) return@launch
        if (!doc.exists()) { settlementRepository.deleteFromRemote(doc.id); return@launch }

        @Suppress("UNCHECKED_CAST")
        val memberMeta = (doc.get("memberMeta") as? Map<String, Map<String, Any?>>).orEmpty()
        val payerKey = doc.getString("payerKey") ?: return@launch
        val receiverKey = doc.getString("receiverKey") ?: return@launch
        val payerId = resolveParticipant(myUid, payerKey, memberMeta) ?: return@launch
        val receiverId = resolveParticipant(myUid, receiverKey, memberMeta) ?: return@launch

        settlementRepository.upsertFromRemote(
            id = doc.id,
            payerFriendId = payerId,
            receiverFriendId = receiverId,
            amountMinor = doc.getLong("amountMinor") ?: 0L,
            date = LocalDate.ofEpochDay(doc.getLong("dateEpochDay") ?: 0L),
            note = doc.getString("note"),
            groupId = doc.getString("groupId")
        )

        val authorUid = doc.getString("lastEditedByUid")
        if (authorUid != null && authorUid != myUid) {
            notificationHelper.notifySyncActivity(
                id = doc.id.hashCode(),
                title = "Settlement recorded",
                text = formatMinor(doc.getLong("amountMinor") ?: 0L)
            )
        }
    }

    /** A participant key is either a real uid (mine, or a linked friend's) or a
     * "local:<ownerUid>:<friendId>" synthetic key — see [FirebaseCloudSyncRepositoryImpl]. */
    private suspend fun resolveParticipant(myUid: String, key: String, memberMeta: Map<String, Map<String, Any?>>): String? {
        if (key == myUid) return YOU_FRIEND_ID
        if (key.startsWith("local:")) {
            val parts = key.removePrefix("local:").split(":", limit = 2)
            // Only resolvable if it's my own synthetic key for one of my own local-only
            // friends — a friend-of-a-friend who isn't linked to my account can't be attributed.
            return if (parts.getOrNull(0) == myUid) parts.getOrNull(1) else null
        }
        return resolveOrCreateFriend(key, memberMeta[key])
    }

    /** Finds the local friend already linked to [uid], or discovers them for the first time —
     * this is what makes adding someone by email a two-way street: once they show up as a
     * member of a shared group/expense, they appear in your friends list automatically too. */
    private suspend fun resolveOrCreateFriend(uid: String, meta: Map<String, Any?>?): String {
        friendRepository.getFriendByLinkedUserId(uid)?.let { return it.id }
        val profile = runCatching { firestore.collection("userDirectory").document(uid).get().await() }.getOrNull()
        val name = profile?.getString("displayName") ?: meta?.get("name") as? String ?: "Friend"
        val email = profile?.getString("email") ?: ""
        val colorArgb = (meta?.get("avatarColorArgb") as? Number)?.toLong() ?: 0xFF3B82F6L
        val created = friendRepository.addFriend(name, email, null, colorArgb, uid)
        return created.id
    }

    private fun formatMinor(amountMinor: Long) = "₹%.2f".format(amountMinor / 100.0)
}
