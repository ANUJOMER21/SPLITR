package com.omer.expensetracker.domain.usecase.budget

import com.omer.expensetracker.domain.model.BudgetProgress
import com.omer.expensetracker.domain.model.BudgetStatus
import com.omer.expensetracker.domain.repository.BudgetRepository
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class BudgetNotificationType { WARNING, BREACHED }

data class BudgetNotificationEvent(val progress: BudgetProgress, val type: BudgetNotificationType)

private val MONTH_KEY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

/**
 * Called by the daily background job. Fires at most one WARNING and one BREACHED event per
 * budget per calendar month — each budget tracks the last month it notified for, so re-running
 * this after every new transaction (or after the app was closed for days) never re-notifies.
 */
class EvaluateBudgetThresholdsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(now: YearMonth = YearMonth.now()): List<BudgetNotificationEvent> {
        val monthKey = now.atDay(1).format(MONTH_KEY_FORMAT)
        val events = mutableListOf<BudgetNotificationEvent>()

        budgetRepository.getBudgetsWithProgressSnapshot(now).forEach { progress ->
            when (progress.status) {
                BudgetStatus.BREACHED -> {
                    if (progress.budget.breachNotifiedMonth != monthKey) {
                        events += BudgetNotificationEvent(progress, BudgetNotificationType.BREACHED)
                        budgetRepository.markBreachNotified(progress.budget.id, monthKey)
                    }
                }
                BudgetStatus.WARNING -> {
                    if (progress.budget.warningNotifiedMonth != monthKey) {
                        events += BudgetNotificationEvent(progress, BudgetNotificationType.WARNING)
                        budgetRepository.markWarningNotified(progress.budget.id, monthKey)
                    }
                }
                BudgetStatus.SAFE -> Unit
            }
        }
        return events
    }
}
