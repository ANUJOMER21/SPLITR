package com.omer.expensetracker.domain.usecase.reminder

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.repository.BillReminderRepository
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.usecase.recurring.RecurrenceDateCalculator
import com.omer.expensetracker.domain.util.UseCaseResult
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class MarkReminderPaidUseCase @Inject constructor(
    private val billReminderRepository: BillReminderRepository,
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(
        id: String,
        autoCreateEntry: Boolean,
        amountMinor: Long? = null,
        paidDate: LocalDate = LocalDate.now()
    ): UseCaseResult<Unit> {
        val reminder = billReminderRepository.getById(id) ?: return UseCaseResult.Failure("Reminder not found")

        var paidEntryId: String? = null
        if (autoCreateEntry) {
            val amount = amountMinor ?: reminder.estimatedAmountMinor
            if (amount == null || amount <= 0L) {
                return UseCaseResult.Failure("Enter an amount to auto-create the expense")
            }
            val now = System.currentTimeMillis()
            val entry = Entry(
                id = UUID.randomUUID().toString(),
                type = EntryType.EXPENSE,
                amountMinor = amount,
                categoryId = reminder.categoryId,
                date = paidDate,
                createdAt = now,
                updatedAt = now
            )
            entryRepository.addEntry(entry)
            paidEntryId = entry.id
        }

        billReminderRepository.markPaid(id, paidEntryId)

        val repeatUnit = reminder.repeatUnit
        val repeatCount = reminder.repeatIntervalCount
        if (repeatUnit != null && repeatCount != null) {
            val nextDue = RecurrenceDateCalculator.advance(reminder.dueDate, repeatUnit, repeatCount)
            billReminderRepository.advanceToNextOccurrence(id, nextDue)
        }
        return UseCaseResult.Success(Unit)
    }
}
