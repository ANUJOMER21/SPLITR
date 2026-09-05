package com.omer.expensetracker.domain.repository

import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.model.RecurrenceUnit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface BillReminderRepository {
    fun observeAll(): Flow<List<BillReminder>>
    suspend fun getById(id: String): BillReminder?

    suspend fun addReminder(
        label: String,
        estimatedAmountMinor: Long?,
        categoryId: String?,
        dueDate: LocalDate,
        leadDays: Int,
        repeatUnit: RecurrenceUnit?,
        repeatIntervalCount: Int?
    ): BillReminder

    suspend fun updateReminder(reminder: BillReminder)
    suspend fun deleteReminder(id: String)
    suspend fun markPaid(id: String, paidEntryId: String?)

    /** Advances a repeating reminder to its next due date, resetting paid status for the new cycle. */
    suspend fun advanceToNextOccurrence(id: String, nextDueDate: LocalDate)

    /** Unpaid reminders whose (dueDate - leadDays) is on or before [today] and haven't yet notified. */
    suspend fun getDueForNotification(today: LocalDate): List<BillReminder>
    suspend fun markNotified(id: String, forDueDate: LocalDate)
}
