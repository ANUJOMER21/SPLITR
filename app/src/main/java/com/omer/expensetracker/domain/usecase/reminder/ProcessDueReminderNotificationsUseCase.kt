package com.omer.expensetracker.domain.usecase.reminder

import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.repository.BillReminderRepository
import java.time.LocalDate
import javax.inject.Inject

/** Called by the daily background job. A reminder only ever notifies once per due date —
 * [BillReminderRepository.getDueForNotification] excludes ones already notified for their
 * current due date, so re-running this daily (or after days away) never re-fires. */
class ProcessDueReminderNotificationsUseCase @Inject constructor(
    private val billReminderRepository: BillReminderRepository
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): List<BillReminder> {
        val due = billReminderRepository.getDueForNotification(today)
        due.forEach { billReminderRepository.markNotified(it.id, it.dueDate) }
        return due
    }
}
