package com.omer.expensetracker.data.repository.sync

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.domain.model.Budget
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.GoalContribution
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.model.SavingsGoal
import com.omer.expensetracker.domain.model.SavingsGoalDetail
import com.omer.expensetracker.domain.model.split.SplitType
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.BudgetRepository
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
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
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringRuleRepository: RecurringRuleRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
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
                .addSnapshotListener { snapshot, _ -> snapshot?.documentChanges?.forEach { onSettlementDoc(uid, it.document) } },
            personalCollection(uid, "categories")
                .addSnapshotListener { s, _ -> s?.documentChanges?.forEach { onCategoryDoc(it.document) } },
            personalCollection(uid, "entries")
                .addSnapshotListener { s, _ -> s?.documentChanges?.forEach { onEntryDoc(it.document) } },
            personalCollection(uid, "budgets")
                .addSnapshotListener { s, _ -> s?.documentChanges?.forEach { onBudgetDoc(it.document) } },
            personalCollection(uid, "recurringRules")
                .addSnapshotListener { s, _ -> s?.documentChanges?.forEach { onRecurringRuleDoc(it.document) } },
            personalCollection(uid, "goals")
                .addSnapshotListener { s, _ -> s?.documentChanges?.forEach { onGoalDoc(it.document) } }
        )
    }

    private fun personalCollection(uid: String, name: String) =
        firestore.collection("users").document(uid).collection(name)

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
            groupId = doc.getString("groupId"),
            batchId = doc.getString("batchId")
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

    // ---- Private per-account data (users/{uid}/<collection>/{id}) ----

    private fun onCategoryDoc(doc: DocumentSnapshot) = scope.launch {
        if (isOwnEcho(doc) || !doc.exists()) return@launch
        categoryRepository.upsertFromRemote(
            Category(
                id = doc.id,
                name = doc.getString("name") ?: return@launch,
                iconKey = doc.getString("iconKey") ?: "other",
                colorArgb = doc.getLong("colorArgb") ?: 0xFF78909CL,
                isDefault = doc.getBoolean("isDefault") ?: false,
                isActive = doc.getBoolean("isActive") ?: true,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        )
    }

    private fun onEntryDoc(doc: DocumentSnapshot) = scope.launch {
        if (isOwnEcho(doc)) return@launch
        if (!doc.exists()) { entryRepository.deleteFromRemote(doc.id); return@launch }
        entryRepository.upsertFromRemote(
            Entry(
                id = doc.id,
                type = runCatching { EntryType.valueOf(doc.getString("type") ?: "EXPENSE") }.getOrDefault(EntryType.EXPENSE),
                amountMinor = doc.getLong("amountMinor") ?: 0L,
                categoryId = doc.getString("categoryId"),
                date = LocalDate.ofEpochDay(doc.getLong("dateEpochDay") ?: 0L),
                note = doc.getString("note"),
                photoUri = doc.getString("photoUri"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        )
        maybeNotify(doc, "New entry", formatMinor(doc.getLong("amountMinor") ?: 0L))
    }

    private fun onBudgetDoc(doc: DocumentSnapshot) = scope.launch {
        if (isOwnEcho(doc)) return@launch
        if (!doc.exists()) { budgetRepository.deleteFromRemote(doc.id); return@launch }
        budgetRepository.upsertFromRemote(
            Budget(
                id = doc.id,
                categoryId = doc.getString("categoryId"),
                monthlyLimitMinor = doc.getLong("monthlyLimitMinor") ?: 0L,
                warningNotifiedMonth = doc.getString("warningNotifiedMonth"),
                breachNotifiedMonth = doc.getString("breachNotifiedMonth"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        )
    }

    private fun onRecurringRuleDoc(doc: DocumentSnapshot) = scope.launch {
        if (isOwnEcho(doc)) return@launch
        if (!doc.exists()) { recurringRuleRepository.deleteFromRemote(doc.id); return@launch }
        recurringRuleRepository.upsertFromRemote(
            RecurringRule(
                id = doc.id,
                type = runCatching { EntryType.valueOf(doc.getString("type") ?: "EXPENSE") }.getOrDefault(EntryType.EXPENSE),
                amountMinor = doc.getLong("amountMinor") ?: 0L,
                categoryId = doc.getString("categoryId"),
                unit = runCatching { RecurrenceUnit.valueOf(doc.getString("unit") ?: "MONTH") }.getOrDefault(RecurrenceUnit.MONTH),
                intervalCount = (doc.getLong("intervalCount") ?: 1L).toInt(),
                startDate = LocalDate.ofEpochDay(doc.getLong("startDateEpochDay") ?: 0L),
                endDate = doc.getLong("endDateEpochDay")?.let { LocalDate.ofEpochDay(it) },
                lastGeneratedDate = doc.getLong("lastGeneratedDateEpochDay")?.let { LocalDate.ofEpochDay(it) },
                nextDueDate = LocalDate.ofEpochDay(doc.getLong("nextDueDateEpochDay") ?: 0L),
                isPaused = doc.getBoolean("isPaused") ?: false,
                skipNextOccurrence = doc.getBoolean("skipNextOccurrence") ?: false,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun onGoalDoc(doc: DocumentSnapshot) = scope.launch {
        if (isOwnEcho(doc)) return@launch
        if (!doc.exists()) { savingsGoalRepository.deleteFromRemote(doc.id); return@launch }
        val goal = SavingsGoal(
            id = doc.id,
            name = doc.getString("name") ?: return@launch,
            targetAmountMinor = doc.getLong("targetAmountMinor") ?: 0L,
            targetDate = doc.getLong("targetDateEpochDay")?.let { LocalDate.ofEpochDay(it) },
            isCompleted = doc.getBoolean("isCompleted") ?: false,
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
        val contributions = (doc.get("contributions") as? List<Map<String, Any?>>).orEmpty().mapNotNull { m ->
            val id = m["id"] as? String ?: return@mapNotNull null
            GoalContribution(
                id = id,
                goalId = doc.id,
                amountMinor = (m["amountMinor"] as? Number)?.toLong() ?: 0L,
                date = LocalDate.ofEpochDay((m["dateEpochDay"] as? Number)?.toLong() ?: 0L),
                createdAt = (m["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (m["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
        savingsGoalRepository.upsertFromRemote(SavingsGoalDetail(goal = goal, contributions = contributions))
    }

    private fun maybeNotify(doc: DocumentSnapshot, title: String, text: String) {
        // Personal-data docs have no author uid; the own-echo check already filtered this device.
        notificationHelper.notifySyncActivity(id = doc.id.hashCode(), title = title, text = text)
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
