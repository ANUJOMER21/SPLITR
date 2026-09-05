package com.omer.expensetracker.domain.usecase.reminder

import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.repository.BillReminderRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class UpdateBillReminderUseCase @Inject constructor(
    private val billReminderRepository: BillReminderRepository
) {
    suspend operator fun invoke(reminder: BillReminder): UseCaseResult<Unit> {
        if (reminder.label.isBlank()) return UseCaseResult.Failure("Label cannot be empty")
        if (reminder.leadDays < 0) return UseCaseResult.Failure("Lead time can't be negative")
        billReminderRepository.updateReminder(reminder.copy(updatedAt = System.currentTimeMillis()))
        return UseCaseResult.Success(Unit)
    }
}
