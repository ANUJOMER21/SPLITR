package com.omer.expensetracker.notification

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Receives the pushes the `notification-server/` Node service sends for shared-expense
 * activity and settlement reminders, and shows them as local notifications. Token registration
 * also happens right after sign-in (see `FirebaseAuthRepositoryImpl`) — this only needs to
 * handle the token *changing* later.
 */
@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        scope.launch {
            runCatching {
                Firebase.firestore.collection("users").document(uid)
                    .collection("fcmTokens").document(token)
                    .set(mapOf("updatedAt" to System.currentTimeMillis())).await()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Expense Tracker"
        val text = message.notification?.body ?: message.data["body"] ?: return
        when (message.data["type"]) {
            "settlement_reminder" -> notificationHelper.notifySettlementReminder(message.hashCode(), title, text)
            else -> notificationHelper.notifySyncActivity(message.hashCode(), title, text)
        }
    }
}
