package com.omer.expensetracker.data.local.entity.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Generic outbox row — one per pending change, keyed by a synthetic id rather than a
 * per-table `sync_status` column, so adding cloud sync didn't require touching every
 * existing entity's schema. */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String? = null
)
