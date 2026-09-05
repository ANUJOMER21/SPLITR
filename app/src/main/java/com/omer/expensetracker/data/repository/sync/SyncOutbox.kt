package com.omer.expensetracker.data.repository.sync

import com.omer.expensetracker.data.local.dao.sync.SyncQueueDao
import com.omer.expensetracker.data.local.entity.sync.SyncQueueEntity
import com.omer.expensetracker.work.SyncScheduler
import javax.inject.Inject
import javax.inject.Singleton

object SyncEntityType {
    const val FRIEND = "friend"
    const val GROUP = "group"
    const val EXPENSE = "expense"
    const val SETTLEMENT = "settlement"

    // Private per-account data — mirrored to users/{uid}/<collection>/{id}.
    const val CATEGORY = "category"
    const val ENTRY = "entry"
    const val BUDGET = "budget"
    const val RECURRING_RULE = "recurringRule"
    const val GOAL = "goal"
}

object SyncOperation {
    const val UPSERT = "UPSERT"
    const val DELETE = "DELETE"
}

/**
 * Records that a local row changed, keyed by "type:id" so repeated edits before the next drain
 * collapse into one pending row rather than piling up duplicates. The outbox itself has nothing
 * to do with [com.omer.expensetracker.FeatureFlags.CLOUD_SYNC_ENABLED] — it's always cheap to
 * write locally; only draining it against Firestore is flag- and sign-in-gated (see
 * [FirebaseCloudSyncRepositoryImpl]).
 */
@Singleton
class SyncOutbox @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val syncScheduler: SyncScheduler
) {
    suspend fun enqueue(entityType: String, entityId: String, operation: String) {
        syncQueueDao.enqueue(
            SyncQueueEntity(
                id = "$entityType:$entityId",
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payloadJson = "",
                createdAt = System.currentTimeMillis()
            )
        )
        syncScheduler.triggerPushSoon()
    }
}
