package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.RecurrenceUnit
import java.time.LocalDate

/**
 * Pure date math shared by recurring entry generation and repeating bill reminders.
 * Deliberately uses [LocalDate.plusMonths] for MONTH steps: it already clamps to the last
 * valid day of the target month (e.g. Jan 31 + 1 month = Feb 28/29), which is exactly the
 * behaviour a "monthly on the 31st" rule needs in short months.
 */
object RecurrenceDateCalculator {

    fun advance(date: LocalDate, unit: RecurrenceUnit, intervalCount: Int): LocalDate {
        require(intervalCount >= 1) { "intervalCount must be >= 1" }
        return when (unit) {
            RecurrenceUnit.DAY -> date.plusDays(intervalCount.toLong())
            RecurrenceUnit.WEEK -> date.plusWeeks(intervalCount.toLong())
            RecurrenceUnit.MONTH -> date.plusMonths(intervalCount.toLong())
        }
    }

    /** Every occurrence date from [from] (exclusive) up to and including [upTo], in order. */
    fun occurrencesUpTo(from: LocalDate, upTo: LocalDate, unit: RecurrenceUnit, intervalCount: Int): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var next = advance(from, unit, intervalCount)
        var guard = 0
        while (!next.isAfter(upTo) && guard < 10_000) {
            result += next
            next = advance(next, unit, intervalCount)
            guard++
        }
        return result
    }
}
