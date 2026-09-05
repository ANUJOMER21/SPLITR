package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense_items", indices = [Index("expenseId")])
data class ExpenseItemEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val name: String,
    val amountMinor: Long
)

@Entity(tableName = "expense_item_shares", primaryKeys = ["itemId", "friendId"], indices = [Index("friendId")])
data class ExpenseItemShareEntity(
    val itemId: String,
    val friendId: String
)
