package com.omer.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("dateEpochDay"), Index("type"), Index("isDeleted"), Index("linkedSharedExpenseId"), Index("linkedGoalContributionId"), Index("photoUri")]
)
data class EntryEntity(
    @PrimaryKey val id: String,
    /** "EXPENSE" or "INCOME" */
    val type: String,
    val amountMinor: Long,
    val categoryId: String?,
    val dateEpochDay: Long,
    /** Free-text label — what the money was for / who it was paid to. Searchable from the
     * entry list. Null or blank when the user didn't add one. */
    val note: String? = null,
    /** Persisted content:// URI of an attached receipt photo, or null. */
    val photoUri: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    /** Set when this entry is your personal share of a Split With Friends expense — kept in
     * sync (amount, date, deletion) by [com.omer.expensetracker.data.repository.split
     * .SharedExpenseRepositoryImpl] whenever that expense changes, so "what you actually spent"
     * shows up in your regular entries and dashboard totals without double-entry. */
    val linkedSharedExpenseId: String? = null,
    /** Set when this entry mirrors a savings-goal contribution — kept in sync (amount, date,
     * deletion) by [com.omer.expensetracker.data.repository.SavingsGoalRepositoryImpl], so money
     * moved into a goal counts as spending on that day under the "Savings" category. */
    val linkedGoalContributionId: String? = null
)
