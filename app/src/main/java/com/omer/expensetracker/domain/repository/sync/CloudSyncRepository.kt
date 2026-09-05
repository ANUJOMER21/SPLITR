package com.omer.expensetracker.domain.repository.sync

import com.omer.expensetracker.domain.model.sync.SyncState
import kotlinx.coroutines.flow.Flow

interface CloudSyncRepository {
    fun observeSyncState(): Flow<SyncState>

    /** Enqueues existing local data for upload. No-op until the Cloud Functions bulk-claim
     * endpoint exists on the backend. */
    suspend fun claimLocalData()

    /** Drains the local outbox against the backend. No-op while [com.omer.expensetracker
     * .FeatureFlags.CLOUD_SYNC_ENABLED] is off, or until the backend half of Phase 5 ships. */
    suspend fun syncNow()
}
