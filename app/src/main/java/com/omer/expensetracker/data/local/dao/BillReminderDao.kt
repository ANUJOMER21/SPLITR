package com.omer.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.BillReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {

    @Query("SELECT * FROM bill_reminders WHERE isDeleted = 0 ORDER BY dueDateEpochDay ASC")
    fun observeAll(): Flow<List<BillReminderEntity>>

    @Query("SELECT * FROM bill_reminders WHERE id = :id")
    suspend fun getById(id: String): BillReminderEntity?

    @Insert
    suspend fun insert(reminder: BillReminderEntity)

    @Insert
    suspend fun insertAll(reminders: List<BillReminderEntity>)

    @Query("SELECT * FROM bill_reminders WHERE isDeleted = 0")
    suspend fun getAllActive(): List<BillReminderEntity>

    @Query("DELETE FROM bill_reminders")
    suspend fun deleteAll()

    @Update
    suspend fun update(reminder: BillReminderEntity)

    @Query("UPDATE bill_reminders SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE bill_reminders SET isPaid = 1, paidEntryId = :paidEntryId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markPaid(id: String, paidEntryId: String?, updatedAt: Long)

    @Query(
        """
        UPDATE bill_reminders
        SET dueDateEpochDay = :nextDueDateEpochDay, isPaid = 0, paidEntryId = NULL,
            lastNotifiedForDueDateEpochDay = NULL, updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun advanceToNextOccurrence(id: String, nextDueDateEpochDay: Long, updatedAt: Long)

    @Query(
        """
        SELECT * FROM bill_reminders
        WHERE isDeleted = 0 AND isPaid = 0
            AND (dueDateEpochDay - leadDays) <= :todayEpochDay
            AND (lastNotifiedForDueDateEpochDay IS NULL OR lastNotifiedForDueDateEpochDay != dueDateEpochDay)
        """
    )
    suspend fun getDueForNotification(todayEpochDay: Long): List<BillReminderEntity>

    @Query("UPDATE bill_reminders SET lastNotifiedForDueDateEpochDay = :dueDateEpochDay, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markNotified(id: String, dueDateEpochDay: Long, updatedAt: Long)
}
