package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense_payers", indices = [Index("expenseId"), Index("friendId")])
data class ExpensePayerEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val friendId: String,
    val paidAmountMinor: Long
)

@Entity(tableName = "expense_splits", indices = [Index("expenseId"), Index("friendId")])
data class ExpenseSplitEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val friendId: String,
    val owedAmountMinor: Long
)
