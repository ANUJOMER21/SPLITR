package com.omer.expensetracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MonthlySummaryTest {

    @Test
    fun `net is income minus expense`() {
        val summary = MonthlySummary(totalExpenseMinor = 4000L, totalIncomeMinor = 10000L)
        assertEquals(6000L, summary.netMinor)
    }

    @Test
    fun `net is negative when expense exceeds income`() {
        val summary = MonthlySummary(totalExpenseMinor = 10000L, totalIncomeMinor = 4000L)
        assertEquals(-6000L, summary.netMinor)
    }

    @Test
    fun `net is zero for no activity`() {
        val summary = MonthlySummary(totalExpenseMinor = 0L, totalIncomeMinor = 0L)
        assertEquals(0L, summary.netMinor)
    }
}
