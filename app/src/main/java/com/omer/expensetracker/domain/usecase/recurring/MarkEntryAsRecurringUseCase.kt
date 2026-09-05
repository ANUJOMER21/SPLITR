package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import java.time.LocalDate
import javax.inject.Inject

/** Turns an existing entry into a recurring rule. The entry itself already counts as the
 * first occurrence, so the rule's clock starts from its date rather than today. */
class MarkEntryAsRecurringUseCase @Inject constructor(
    private val entryRepository: EntryRepository,
    private val recurringRuleRepository: RecurringRuleRepository
) {
    suspend operator fun invoke(
        entryId: String,
        unit: RecurrenceUnit,
        intervalCount: Int,
        endDate: LocalDate?
    ): UseCaseResult<RecurringRule> {
        val entry = entryRepository.getEntry(entryId) ?: return UseCaseResult.Failure("Entry not found")
        if (intervalCount < 1) return UseCaseResult.Failure("Interval must be at least 1")
        if (endDate != null && endDate.isBefore(entry.date)) {
            return UseCaseResult.Failure("End date must be after the entry's date")
        }
        val rule = recurringRuleRepository.addRule(
            type = entry.type,
            amountMinor = entry.amountMinor,
            categoryId = entry.categoryId,
            unit = unit,
            intervalCount = intervalCount,
            startDate = entry.date,
            endDate = endDate
        )
        val nextDue = RecurrenceDateCalculator.advance(entry.date, unit, intervalCount)
        recurringRuleRepository.recordGenerated(rule.id, generatedDate = entry.date, nextDueDate = nextDue)
        return UseCaseResult.Success(rule.copy(lastGeneratedDate = entry.date, nextDueDate = nextDue))
    }
}
