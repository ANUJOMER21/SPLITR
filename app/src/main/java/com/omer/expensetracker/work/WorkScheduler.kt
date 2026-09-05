package com.omer.expensetracker.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val PERIODIC_WORK_NAME = "daily_maintenance"
private const val CATCH_UP_WORK_NAME = "daily_maintenance_catch_up"

/** Schedules [DailyMaintenanceWorker] to run at least once a day, plus fires an immediate
 * catch-up run on every app launch (a device that was off, or an app that was force-stopped,
 * can miss the periodic schedule entirely — launch is the one moment guaranteed to happen). */
@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleDaily() {
        val request = PeriodicWorkRequestBuilder<DailyMaintenanceWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun runCatchUpNow() {
        val request = OneTimeWorkRequestBuilder<DailyMaintenanceWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(CATCH_UP_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
