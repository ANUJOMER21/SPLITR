package com.omer.expensetracker.domain.usecase.reminder

import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.repository.BillReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBillRemindersUseCase @Inject constructor(
    private val billReminderRepository: BillReminderRepository
) {
    operator fun invoke(): Flow<List<BillReminder>> = billReminderRepository.observeAll()
}
