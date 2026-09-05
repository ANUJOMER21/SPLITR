package com.omer.expensetracker.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val PUSH_WORK_NAME = "cloud_sync_push"
private const val PERIODIC_WORK_NAME = "cloud_sync_periodic"

/**
 * Cloud sync is entirely background-triggered — there is no manual "sync now" button anywhere
 * in the UI. [SyncOutbox] calls [triggerPushSoon] right after every local write; this schedules
 * [CloudSyncWorker] with network constraints so it runs the moment connectivity is available,
 * coalescing bursts of edits into one drain (APPEND_OR_REPLACE on a unique work name). The
 * periodic job is only a backstop for edits made while this device never regained connectivity
 * long enough for the one-time job to fire.
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val networkConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun triggerPushSoon() {
        val request = OneTimeWorkRequestBuilder<CloudSyncWorker>().setConstraints(networkConstraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork(PUSH_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<CloudSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
