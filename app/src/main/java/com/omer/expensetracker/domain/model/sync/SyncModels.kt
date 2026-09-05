package com.omer.expensetracker.domain.model.sync

enum class SyncStatus { SIGNED_OUT, SIGNED_IN, SYNCING, ERROR }

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?
)

data class SyncState(
    val status: SyncStatus = SyncStatus.SIGNED_OUT,
    val user: AuthUser? = null,
    val pendingUploads: Int = 0,
    val lastSyncedAt: Long? = null,
    val lastError: String? = null
)
