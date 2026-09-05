package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import java.time.LocalDate
import javax.inject.Inject

class AddRecurringRuleUseCase @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository
) {
    suspend operator fun invoke(
        type: EntryType,
        amountMinor: Long,
        categoryId: String?,
        unit: RecurrenceUnit,
        intervalCount: Int,
        startDate: LocalDate,
        endDate: LocalDate?
    ): UseCaseResult<RecurringRule> {
        if (amountMinor <= 0L) return UseCaseResult.Failure("Amount must be greater than zero")
        if (type == EntryType.EXPENSE && categoryId.isNullOrBlank()) {
            return UseCaseResult.Failure("Category is required for an expense")
        }
        if (intervalCount < 1) return UseCaseResult.Failure("Interval must be at least 1")
        if (endDate != null && endDate.isBefore(startDate)) {
            return UseCaseResult.Failure("End date must be after the start date")
        }
        return UseCaseResult.Success(
            recurringRuleRepository.addRule(type, amountMinor, categoryId, unit, intervalCount, startDate, endDate)
        )
    }
}
