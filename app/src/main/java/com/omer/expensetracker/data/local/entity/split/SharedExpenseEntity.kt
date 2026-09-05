package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shared_expenses",
    indices = [Index("groupId"), Index("categoryId"), Index("isDeleted"), Index("dateEpochDay")]
)
data class SharedExpenseEntity(
    @PrimaryKey val id: String,
    val description: String,
    val amountMinor: Long,
    val categoryId: String?,
    val splitType: String,
    val dateEpochDay: Long,
    val groupId: String?,
    val photoUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
