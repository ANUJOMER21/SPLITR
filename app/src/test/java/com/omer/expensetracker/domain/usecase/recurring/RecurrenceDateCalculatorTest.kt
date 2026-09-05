package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.RecurrenceUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RecurrenceDateCalculatorTest {

    @Test
    fun `monthly on the 31st lands on the last day of a shorter month`() {
        val jan31 = LocalDate.of(2026, 1, 31)
        val next = RecurrenceDateCalculator.advance(jan31, RecurrenceUnit.MONTH, 1)
        assertEquals(LocalDate.of(2026, 2, 28), next)
    }

    @Test
    fun `monthly on the 31st lands on Feb 29 in a leap year`() {
        val jan31 = LocalDate.of(2028, 1, 31)
        val next = RecurrenceDateCalculator.advance(jan31, RecurrenceUnit.MONTH, 1)
        assertEquals(LocalDate.of(2028, 2, 29), next)
    }

    @Test
    fun `monthly on the 31st does not permanently clamp to 28`() {
        // Feb 28 + 1 month should land on Mar 28, not stay clamped from the previous step.
        val feb28 = LocalDate.of(2026, 2, 28)
        val next = RecurrenceDateCalculator.advance(feb28, RecurrenceUnit.MONTH, 1)
        assertEquals(LocalDate.of(2026, 3, 28), next)
    }

    @Test
    fun `weekly advances by exactly 7 days per interval`() {
        val start = LocalDate.of(2026, 1, 1)
        val next = RecurrenceDateCalculator.advance(start, RecurrenceUnit.WEEK, 2)
        assertEquals(LocalDate.of(2026, 1, 15), next)
    }

    @Test
    fun `daily advances by exactly N days`() {
        val start = LocalDate.of(2026, 1, 1)
        val next = RecurrenceDateCalculator.advance(start, RecurrenceUnit.DAY, 10)
        assertEquals(LocalDate.of(2026, 1, 11), next)
    }

    @Test
    fun `occurrencesUpTo returns every missed occurrence after a long gap, in order`() {
        val lastGenerated = LocalDate.of(2026, 1, 1)
        val today = LocalDate.of(2026, 4, 1)
        val occurrences = RecurrenceDateCalculator.occurrencesUpTo(lastGenerated, today, RecurrenceUnit.MONTH, 1)
        assertEquals(
            listOf(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1)),
            occurrences
        )
    }

    @Test
    fun `occurrencesUpTo is empty when nothing is due yet`() {
        val lastGenerated = LocalDate.of(2026, 4, 1)
        val today = LocalDate.of(2026, 3, 15)
        assertEquals(emptyList<LocalDate>(), RecurrenceDateCalculator.occurrencesUpTo(lastGenerated, today, RecurrenceUnit.MONTH, 1))
    }
}
