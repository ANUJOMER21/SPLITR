package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlements",
    indices = [Index("payerFriendId"), Index("receiverFriendId"), Index("groupId"), Index("isDeleted")]
)
data class SettlementEntity(
    @PrimaryKey val id: String,
    val payerFriendId: String,
    val receiverFriendId: String,
    val amountMinor: Long,
    val dateEpochDay: Long,
    val note: String?,
    val groupId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
