package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.BillReminderDao
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.mapper.toEntity
import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.repository.BillReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class BillReminderRepositoryImpl @Inject constructor(
    private val billReminderDao: BillReminderDao
) : BillReminderRepository {

    override fun observeAll(): Flow<List<BillReminder>> =
        billReminderDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): BillReminder? = billReminderDao.getById(id)?.toDomain()

    override suspend fun addReminder(
        label: String,
        estimatedAmountMinor: Long?,
        categoryId: String?,
        dueDate: LocalDate,
        leadDays: Int,
        repeatUnit: RecurrenceUnit?,
        repeatIntervalCount: Int?
    ): BillReminder {
        val now = System.currentTimeMillis()
        val reminder = BillReminder(
            id = UUID.randomUUID().toString(),
            label = label,
            estimatedAmountMinor = estimatedAmountMinor,
            categoryId = categoryId,
            dueDate = dueDate,
            leadDays = leadDays,
            repeatUnit = repeatUnit,
            repeatIntervalCount = repeatIntervalCount,
            isPaid = false,
            paidEntryId = null,
            lastNotifiedForDueDate = null,
            createdAt = now,
            updatedAt = now
        )
        billReminderDao.insert(reminder.toEntity())
        return reminder
    }

    override suspend fun updateReminder(reminder: BillReminder) {
        billReminderDao.update(reminder.toEntity())
    }

    override suspend fun deleteReminder(id: String) {
        billReminderDao.softDelete(id, System.currentTimeMillis())
    }

    override suspend fun markPaid(id: String, paidEntryId: String?) {
        billReminderDao.markPaid(id, paidEntryId, System.currentTimeMillis())
    }

    override suspend fun advanceToNextOccurrence(id: String, nextDueDate: LocalDate) {
        billReminderDao.advanceToNextOccurrence(id, nextDueDate.toEpochDay(), System.currentTimeMillis())
    }

    override suspend fun getDueForNotification(today: LocalDate): List<BillReminder> =
        billReminderDao.getDueForNotification(today.toEpochDay()).map { it.toDomain() }

    override suspend fun markNotified(id: String, forDueDate: LocalDate) {
        billReminderDao.markNotified(id, forDueDate.toEpochDay(), System.currentTimeMillis())
    }
}
