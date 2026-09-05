package com.omer.expensetracker.domain.model

import java.time.LocalDate

enum class InsightsPeriod { WEEKLY, MONTHLY, YEARLY }

data class TrendPoint(
    val periodLabel: String,
    val periodStart: LocalDate,
    val expenseMinor: Long,
    val incomeMinor: Long
)

data class TopCategoryInsight(
    val category: Category,
    val totalMinor: Long,
    val percentage: Float
)

/** dayOfWeek: 1=Monday .. 7=Sunday (ISO), matching [java.time.DayOfWeek.getValue]. */
data class HeatmapCell(
    val dayOfWeek: Int,
    val totalMinor: Long
)

data class MonthOverMonthChange(
    val category: Category?,
    val currentMonthMinor: Long,
    val previousMonthMinor: Long
) {
    val percentageChange: Float?
        get() = if (previousMonthMinor == 0L) null
        else (currentMonthMinor - previousMonthMinor) * 100f / previousMonthMinor
}

data class SavingsRatePoint(
    val periodLabel: String,
    val periodStart: LocalDate,
    val incomeMinor: Long,
    val expenseMinor: Long
) {
    /** Null when there was no income that period — a rate would be meaningless. */
    val rate: Float? get() = if (incomeMinor > 0) (incomeMinor - expenseMinor).toFloat() / incomeMinor else null
}

data class AnomalyFlag(
    val entry: Entry,
    val category: Category?,
    val categoryAverageMinor: Long
)
