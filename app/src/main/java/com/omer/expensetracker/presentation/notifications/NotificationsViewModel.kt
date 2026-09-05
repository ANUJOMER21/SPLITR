package com.omer.expensetracker.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import com.omer.expensetracker.domain.model.BudgetStatus
import com.omer.expensetracker.domain.repository.BillReminderRepository
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import com.omer.expensetracker.domain.usecase.budget.GetBudgetsWithProgressUseCase
import com.omer.expensetracker.presentation.util.formatAsCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Surfaces the same things the system notifications warn about — overdue/upcoming bills,
 * budgets running hot, goals falling behind pace — as a live in-app list, so nothing is
 * missed if a system notification was dismissed, delayed, or never granted permission. */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    billReminderRepository: BillReminderRepository,
    savingsGoalRepository: SavingsGoalRepository,
    getBudgetsWithProgressUseCase: GetBudgetsWithProgressUseCase
) : ViewModel() {

    val uiState: StateFlow<NotificationsUiState> = combine(
        billReminderRepository.observeAll(),
        getBudgetsWithProgressUseCase(YearMonth.now()),
        savingsGoalRepository.observeGoals()
    ) { reminders, budgets, goals ->
        val today = LocalDate.now()
        val items = buildList {
            reminders.filter { !it.isPaid && it.dueDate.minusDays(it.leadDays.toLong()) <= today }
                .sortedBy { it.dueDate }
                .forEach { reminder ->
                    val overdue = reminder.isOverdueUnpaid(today)
                    add(
                        NotificationItem(
                            id = "reminder_${reminder.id}",
                            icon = Icons.Filled.NotificationsActive,
                            title = reminder.label,
                            subtitle = if (overdue) {
                                "Overdue since ${reminder.dueDate}"
                            } else {
                                "Due ${reminder.dueDate}" + (reminder.estimatedAmountMinor?.let { " · ${it.formatAsCurrency()}" } ?: "")
                            },
                            severity = if (overdue) NotificationSeverity.CRITICAL else NotificationSeverity.WARNING
                        )
                    )
                }

            budgets.filter { it.status != BudgetStatus.SAFE }.forEach { progress ->
                val label = progress.category?.name ?: "Overall budget"
                add(
                    NotificationItem(
                        id = "budget_${progress.budget.id}",
                        icon = Icons.Filled.ErrorOutline,
                        title = if (progress.status == BudgetStatus.BREACHED) "$label is over budget" else "$label is close to its limit",
                        subtitle = "${progress.spentMinor.formatAsCurrency()} of ${progress.limitMinor.formatAsCurrency()} spent",
                        severity = if (progress.status == BudgetStatus.BREACHED) NotificationSeverity.CRITICAL else NotificationSeverity.WARNING
                    )
                )
            }

            goals.filter { !it.goal.isCompleted && it.isOnTrack == false && it.goal.targetDate != null }
                .forEach { detail ->
                    val daysLeft = ChronoUnit.DAYS.between(today, detail.goal.targetDate)
                    add(
                        NotificationItem(
                            id = "goal_${detail.goal.id}",
                            icon = Icons.Filled.Savings,
                            title = "\"${detail.goal.name}\" is behind pace",
                            subtitle = if (daysLeft < 0) "Target date has passed" else "${detail.remainingMinor.formatAsCurrency()} left, $daysLeft days to go",
                            severity = NotificationSeverity.WARNING
                        )
                    )
                }
        }
        NotificationsUiState(items = items, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationsUiState())
}
