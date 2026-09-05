package com.omer.expensetracker.data.repository.sync

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.domain.repository.sync.ReminderRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes a `settlementReminders/{id}` doc; a Cloud Function (see `functions/index.ts`) picks it
 * up on create and sends the actual FCM push to [toUserId]'s device(s). This app never talks to
 * FCM's send API directly — that requires a server credential that must never ship on-device.
 */
@Singleton
class FirestoreReminderRepositoryImpl @Inject constructor() : ReminderRepository {

    private val firestore by lazy { Firebase.firestore }

    override suspend fun sendSettlementReminder(toUserId: String, friendName: String, amountMinor: Long, groupId: String?): Result<Unit> {
        if (!FeatureFlags.CLOUD_SYNC_ENABLED) return Result.failure(IllegalStateException("Cloud sync is disabled"))
        val fromUid = Firebase.auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return runCatching {
            firestore.collection("settlementReminders").document(UUID.randomUUID().toString()).set(
                mapOf(
                    "fromUid" to fromUid, "toUid" to toUserId, "amountMinor" to amountMinor,
                    "groupId" to groupId, "createdAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }
}
