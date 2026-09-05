package com.omer.expensetracker.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import com.omer.expensetracker.domain.usecase.budget.BudgetNotificationType
import com.omer.expensetracker.domain.usecase.budget.EvaluateBudgetThresholdsUseCase
import com.omer.expensetracker.domain.usecase.recurring.GenerateDueRecurringEntriesUseCase
import com.omer.expensetracker.domain.usecase.reminder.ProcessDueReminderNotificationsUseCase
import com.omer.expensetracker.notification.NotificationHelper
import com.omer.expensetracker.presentation.util.formatAsCurrency
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Runs at least daily (scheduled by [WorkScheduler]) plus once as a catch-up on every app
 * launch. Idempotent by design: recurring generation and reminder notification both key off
 * a "last processed" marker in the database, so running this repeatedly — or after the app
 * was closed for days — never duplicates an entry or a notification.
 */
@HiltWorker
class DailyMaintenanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val generateDueRecurringEntriesUseCase: GenerateDueRecurringEntriesUseCase,
    private val processDueReminderNotificationsUseCase: ProcessDueReminderNotificationsUseCase,
    private val evaluateBudgetThresholdsUseCase: EvaluateBudgetThresholdsUseCase,
    private val categoryRepository: CategoryRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val generated = generateDueRecurringEntriesUseCase()
        generated.forEach { event ->
            val categoryName = event.rule.categoryId?.let { categoryRepository.getCategory(it)?.name }
            notificationHelper.notifyRecurring(
                id = event.entry.id.hashCode(),
                title = "Recurring entry added",
                text = buildString {
                    append(event.entry.amountMinor.formatAsCurrency())
                    categoryName?.let { append(" · $it") }
                    append(" on ${event.entry.date.format(DateTimeFormatter.ofPattern("dd MMM"))}")
                }
            )
        }

        val dueReminders = processDueReminderNotificationsUseCase()
        dueReminders.forEach { reminder ->
            notificationHelper.notifyReminder(
                id = reminder.id.hashCode(),
                title = reminder.label,
                text = "Due ${reminder.dueDate.format(DateTimeFormatter.ofPattern("dd MMM"))}"
            )
        }

        val budgetEvents = evaluateBudgetThresholdsUseCase()
        budgetEvents.forEach { event ->
            val label = event.progress.category?.name ?: "Overall budget"
            val title = if (event.type == BudgetNotificationType.BREACHED) "$label is over budget" else "$label is near its limit"
            notificationHelper.notifyBudget(
                id = event.progress.budget.id.hashCode(),
                title = title,
                text = "${event.progress.spentMinor.formatAsCurrency()} of ${event.progress.limitMinor.formatAsCurrency()} spent"
            )
        }

        val today = LocalDate.now()
        savingsGoalRepository.observeGoals().first().forEach { detail ->
            val targetDate = detail.goal.targetDate ?: return@forEach
            if (detail.goal.isCompleted || detail.isOnTrack != false) return@forEach
            val daysLeft = ChronoUnit.DAYS.between(today, targetDate)
            if (daysLeft > 14) return@forEach
            notificationHelper.notifyGoal(
                id = detail.goal.id.hashCode(),
                title = "\"${detail.goal.name}\" is behind pace",
                text = if (daysLeft < 0) "Target date has passed" else "${detail.remainingMinor.formatAsCurrency()} left with $daysLeft days to go"
            )
        }

        return Result.success()
    }
}
