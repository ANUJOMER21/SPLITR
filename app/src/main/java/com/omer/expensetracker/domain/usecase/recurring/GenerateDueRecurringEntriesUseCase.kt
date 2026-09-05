package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class GeneratedRecurringEntry(val rule: RecurringRule, val entry: Entry)

/**
 * Called by the daily background job (and on app launch as a catch-up check). For each due
 * rule, walks every occurrence from its current `nextDueDate` up to today — not just one —
 * so a rule left untouched for several days generates each missed occurrence exactly once,
 * never a duplicate, whether this runs daily or after a long gap.
 */
class GenerateDueRecurringEntriesUseCase @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository,
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): List<GeneratedRecurringEntry> {
        val generated = mutableListOf<GeneratedRecurringEntry>()

        recurringRuleRepository.getDueRules(today).forEach { rule ->
            val occurrences = generateSequence(rule.nextDueDate) {
                RecurrenceDateCalculator.advance(it, rule.unit, rule.intervalCount)
            }.takeWhile { date ->
                !date.isAfter(today) && (rule.endDate == null || !date.isAfter(rule.endDate))
            }.toList()

            if (occurrences.isEmpty()) return@forEach

            var skipRemaining = rule.skipNextOccurrence
            occurrences.forEachIndexed { index, date ->
                if (index == 0 && skipRemaining) {
                    skipRemaining = false
                } else {
                    val now = System.currentTimeMillis()
                    val entry = Entry(
                        id = UUID.randomUUID().toString(),
                        type = rule.type,
                        amountMinor = rule.amountMinor,
                        categoryId = rule.categoryId,
                        date = date,
                        createdAt = now,
                        updatedAt = now
                    )
                    entryRepository.addEntry(entry)
                    generated += GeneratedRecurringEntry(rule, entry)
                }
            }

            val lastOccurrence = occurrences.last()
            recurringRuleRepository.recordGenerated(
                id = rule.id,
                generatedDate = lastOccurrence,
                nextDueDate = RecurrenceDateCalculator.advance(lastOccurrence, rule.unit, rule.intervalCount)
            )
            if (rule.skipNextOccurrence) {
                recurringRuleRepository.setSkipNextOccurrence(rule.id, false)
            }
        }

        return generated
    }
}
