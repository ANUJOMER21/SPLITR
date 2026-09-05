package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Pausing stops the rule from being picked up by the generator. Resuming fast-forwards
 * [nextDueDate][com.omer.expensetracker.domain.model.RecurringRule.nextDueDate] past whatever
 * occurrences fell inside the paused window instead of catch-up-generating a burst of them —
 * pausing was a deliberate "skip this period," not downtime to backfill.
 */
class SetRecurringRulePausedUseCase @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository
) {
    suspend operator fun invoke(id: String, isPaused: Boolean, today: LocalDate = LocalDate.now()) {
        recurringRuleRepository.setPaused(id, isPaused)
        if (isPaused) return

        val rule = recurringRuleRepository.getById(id) ?: return
        if (rule.nextDueDate.isAfter(today) || rule.nextDueDate.isEqual(today)) return

        var nextDue = rule.nextDueDate
        var guard = 0
        while (nextDue.isBefore(today) && guard < 10_000) {
            nextDue = RecurrenceDateCalculator.advance(nextDue, rule.unit, rule.intervalCount)
            guard++
        }
        recurringRuleRepository.updateRule(rule.copy(nextDueDate = nextDue))
    }
}
