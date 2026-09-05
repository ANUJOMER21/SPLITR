package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "activity_log", indices = [Index("groupId"), Index("timestamp")])
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val groupId: String?,
    val type: String,
    val summary: String,
    val timestamp: Long
)
