package com.omer.expensetracker.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omer.expensetracker.domain.repository.sync.CloudSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Drains the local outbox against Firestore — see [SyncScheduler] for when this runs. Never
 * fails hard: a push that doesn't go through (offline, a transient Firestore error) just stays
 * queued and is retried on the next trigger. */
@HiltWorker
class CloudSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cloudSyncRepository: CloudSyncRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        cloudSyncRepository.syncNow()
        return Result.success()
    }
}
