package com.omer.expensetracker.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.omer.expensetracker.MainActivity
import com.omer.expensetracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_BUDGETS = "budget_alerts"
private const val CHANNEL_RECURRING = "recurring_entries"
private const val CHANNEL_REMINDERS = "bill_reminders"
private const val CHANNEL_GOALS = "savings_goals"
private const val CHANNEL_SYNC_ACTIVITY = "sync_activity"
private const val CHANNEL_SETTLEMENT_REMINDERS = "settlement_reminders"

/** Every notification this app ever posts goes through here. Callers pass a stable [id] per
 * logical event (e.g. a budget's id, a reminder's id) so re-posting the same event updates
 * rather than duplicates it. */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_BUDGETS, "Budget alerts", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_RECURRING, "Recurring entries", NotificationManager.IMPORTANCE_LOW)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Bill reminders", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_GOALS, "Savings goals", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SYNC_ACTIVITY, "Shared activity", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SETTLEMENT_REMINDERS, "Settlement reminders", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    fun notifyBudget(id: Int, title: String, text: String) = post(id, CHANNEL_BUDGETS, title, text)
    fun notifyRecurring(id: Int, title: String, text: String) = post(id, CHANNEL_RECURRING, title, text)
    fun notifyReminder(id: Int, title: String, text: String) = post(id, CHANNEL_REMINDERS, title, text)
    fun notifyGoal(id: Int, title: String, text: String) = post(id, CHANNEL_GOALS, title, text)
    fun notifySyncActivity(id: Int, title: String, text: String) = post(id, CHANNEL_SYNC_ACTIVITY, title, text)
    fun notifySettlementReminder(id: Int, title: String, text: String) = post(id, CHANNEL_SETTLEMENT_REMINDERS, title, text)

    private fun post(id: Int, channelId: String, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(
                android.app.PendingIntent.getActivity(
                    context, id,
                    android.content.Intent(context, MainActivity::class.java),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
