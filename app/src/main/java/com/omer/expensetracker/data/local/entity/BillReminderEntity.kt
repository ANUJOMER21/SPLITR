package com.omer.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "bill_reminders",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["paidEntryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("paidEntryId"), Index("dueDateEpochDay"), Index("isDeleted")]
)
data class BillReminderEntity(
    @PrimaryKey val id: String,
    val label: String,
    val estimatedAmountMinor: Long?,
    val categoryId: String?,
    val dueDateEpochDay: Long,
    val leadDays: Int,
    val repeatUnit: String?,
    val repeatIntervalCount: Int?,
    val isPaid: Boolean,
    val paidEntryId: String?,
    val lastNotifiedForDueDateEpochDay: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
