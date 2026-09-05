package com.omer.expensetracker.domain.repository.sync

/** Pings a friend's device with a "settle up" push. Only works for a linked friend (real
 * account) — the request is a Firestore write that a backend Cloud Function turns into an FCM
 * push to their registered device(s); this app never talks to FCM's send API directly. */
interface ReminderRepository {
    suspend fun sendSettlementReminder(toUserId: String, friendName: String, amountMinor: Long, groupId: String?): Result<Unit>
}
