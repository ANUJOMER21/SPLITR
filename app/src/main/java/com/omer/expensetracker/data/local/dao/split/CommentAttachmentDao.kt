package com.omer.expensetracker.data.local.dao.split

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.omer.expensetracker.data.local.entity.split.ExpenseAttachmentEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentAttachmentDao {
    @Query("SELECT * FROM expense_comments WHERE expenseId = :expenseId ORDER BY createdAt ASC")
    fun observeComments(expenseId: String): Flow<List<ExpenseCommentEntity>>

    @Insert
    suspend fun insertComment(comment: ExpenseCommentEntity)

    @Query("SELECT * FROM expense_attachments WHERE expenseId = :expenseId ORDER BY createdAt ASC")
    fun observeAttachments(expenseId: String): Flow<List<ExpenseAttachmentEntity>>

    @Insert
    suspend fun insertAttachment(attachment: ExpenseAttachmentEntity)
}
