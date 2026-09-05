package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "friend_groups", indices = [Index("isDeleted"), Index("isArchived")])
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)

@Entity(tableName = "group_members", primaryKeys = ["groupId", "friendId"], indices = [Index("friendId")])
data class GroupMemberEntity(
    val groupId: String,
    val friendId: String,
    val joinedAt: Long
)
