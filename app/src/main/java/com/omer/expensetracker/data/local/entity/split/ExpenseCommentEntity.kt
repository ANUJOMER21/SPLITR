package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense_comments", indices = [Index("expenseId")])
data class ExpenseCommentEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val text: String,
    val createdAt: Long
)

@Entity(tableName = "expense_attachments", indices = [Index("expenseId")])
data class ExpenseAttachmentEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val uri: String,
    val createdAt: Long
)
