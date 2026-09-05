package com.omer.expensetracker.domain.usecase.reminder

import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.repository.BillReminderRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import java.time.LocalDate
import javax.inject.Inject

class AddBillReminderUseCase @Inject constructor(
    private val billReminderRepository: BillReminderRepository
) {
    suspend operator fun invoke(
        label: String,
        estimatedAmountMinor: Long?,
        categoryId: String?,
        dueDate: LocalDate,
        leadDays: Int,
        repeatUnit: RecurrenceUnit?,
        repeatIntervalCount: Int?
    ): UseCaseResult<BillReminder> {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return UseCaseResult.Failure("Label cannot be empty")
        if (leadDays < 0) return UseCaseResult.Failure("Lead time can't be negative")
        if (estimatedAmountMinor != null && estimatedAmountMinor <= 0L) {
            return UseCaseResult.Failure("Estimated amount must be greater than zero")
        }
        if (repeatUnit != null && (repeatIntervalCount == null || repeatIntervalCount < 1)) {
            return UseCaseResult.Failure("Repeat interval must be at least 1")
        }
        return UseCaseResult.Success(
            billReminderRepository.addReminder(
                trimmed, estimatedAmountMinor, categoryId, dueDate, leadDays, repeatUnit, repeatIntervalCount
            )
        )
    }
}
