package com.omer.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("isDeleted")]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    /** Null means the overall monthly cap, independent of any single category. */
    val categoryId: String?,
    val monthlyLimitMinor: Long,
    /** "yyyy-MM" of the last month a warning/breach notification fired, or null. */
    val warningNotifiedMonth: String?,
    val breachNotifiedMonth: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
