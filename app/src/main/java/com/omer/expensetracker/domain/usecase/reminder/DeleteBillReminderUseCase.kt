package com.omer.expensetracker.domain.usecase.reminder

import com.omer.expensetracker.domain.repository.BillReminderRepository
import javax.inject.Inject

class DeleteBillReminderUseCase @Inject constructor(
    private val billReminderRepository: BillReminderRepository
) {
    suspend operator fun invoke(id: String) = billReminderRepository.deleteReminder(id)
}
