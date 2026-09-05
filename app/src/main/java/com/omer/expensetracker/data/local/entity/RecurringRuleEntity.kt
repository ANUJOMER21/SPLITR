package com.omer.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("nextDueDateEpochDay"), Index("isDeleted")]
)
data class RecurringRuleEntity(
    @PrimaryKey val id: String,
    val type: String,
    val amountMinor: Long,
    val categoryId: String?,
    /** "DAY", "WEEK", or "MONTH" — see [com.omer.expensetracker.domain.model.RecurrenceUnit]. */
    val unit: String,
    val intervalCount: Int,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long?,
    val lastGeneratedDateEpochDay: Long?,
    val nextDueDateEpochDay: Long,
    val isPaused: Boolean,
    val skipNextOccurrence: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
