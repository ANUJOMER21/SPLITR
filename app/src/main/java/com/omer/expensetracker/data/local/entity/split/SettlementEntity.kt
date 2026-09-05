package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlements",
    indices = [Index("payerFriendId"), Index("receiverFriendId"), Index("groupId"), Index("batchId"), Index("isDeleted")]
)
data class SettlementEntity(
    @PrimaryKey val id: String,
    val payerFriendId: String,
    val receiverFriendId: String,
    val amountMinor: Long,
    val dateEpochDay: Long,
    val note: String?,
    val groupId: String?,
    /** Non-null when this row is one slice of a single friend-level settlement that was split
     * across several groups (+ the non-group bucket); all slices share the same [batchId] so
     * edit/delete can act on the whole payment. Null for a plain single-bucket settlement. */
    val batchId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
