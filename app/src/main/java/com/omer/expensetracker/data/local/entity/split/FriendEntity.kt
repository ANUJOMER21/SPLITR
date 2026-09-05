package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "friends", indices = [Index("isDeleted"), Index("linkedUserId")])
data class FriendEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val contactInfo: String?,
    val avatarColorArgb: Long,
    val isYou: Boolean,
    val linkedUserId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
