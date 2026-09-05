package com.omer.expensetracker.domain.usecase.reminder

import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.repository.BillReminderRepository
import javax.inject.Inject

class GetBillReminderUseCase @Inject constructor(
    private val billReminderRepository: BillReminderRepository
) {
    suspend operator fun invoke(id: String): BillReminder? = billReminderRepository.getById(id)
}
