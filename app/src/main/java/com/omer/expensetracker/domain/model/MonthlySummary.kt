package com.omer.expensetracker.domain.model

data class MonthlySummary(
    val totalExpenseMinor: Long,
    val totalIncomeMinor: Long
) {
    val netMinor: Long get() = totalIncomeMinor - totalExpenseMinor
}

data class CategoryBreakdownItem(
    val category: Category,
    val totalMinor: Long,
    val percentage: Float
)
