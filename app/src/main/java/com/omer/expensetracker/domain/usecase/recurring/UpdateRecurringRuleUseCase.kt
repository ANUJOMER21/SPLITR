package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

/** Only ever changes future occurrences — entries already generated from this rule are plain
 * [com.omer.expensetracker.domain.model.Entry] rows with no back-reference, so editing the
 * rule here cannot retroactively touch them. */
class UpdateRecurringRuleUseCase @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository
) {
    suspend operator fun invoke(rule: RecurringRule): UseCaseResult<Unit> {
        if (rule.amountMinor <= 0L) return UseCaseResult.Failure("Amount must be greater than zero")
        if (rule.type == EntryType.EXPENSE && rule.categoryId.isNullOrBlank()) {
            return UseCaseResult.Failure("Category is required for an expense")
        }
        if (rule.intervalCount < 1) return UseCaseResult.Failure("Interval must be at least 1")
        if (rule.endDate != null && rule.endDate.isBefore(rule.startDate)) {
            return UseCaseResult.Failure("End date must be after the start date")
        }
        recurringRuleRepository.updateRule(rule.copy(updatedAt = System.currentTimeMillis()))
        return UseCaseResult.Success(Unit)
    }
}
