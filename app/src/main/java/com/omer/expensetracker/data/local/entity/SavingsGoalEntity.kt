package com.omer.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "savings_goals", indices = [Index("isDeleted")])
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetAmountMinor: Long,
    val targetDateEpochDay: Long?,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
