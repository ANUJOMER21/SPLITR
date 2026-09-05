package com.omer.expensetracker.presentation.dashboard

import com.omer.expensetracker.domain.model.BudgetProgress
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.CategoryBreakdownItem
import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.MonthOverMonthChange
import com.omer.expensetracker.domain.model.MonthlySummary
import java.time.YearMonth

data class DashboardUiState(
    val month: YearMonth = YearMonth.now(),
    val summary: MonthlySummary = MonthlySummary(0, 0),
    val breakdown: List<CategoryBreakdownItem> = emptyList(),
    val budgets: List<BudgetProgress> = emptyList(),
    val overallMoM: MonthOverMonthChange? = null,
    val sharedNetMinor: Long = 0L,
    val sharedFriendCount: Int = 0,
    val recentEntries: List<Entry> = emptyList(),
    val categoriesById: Map<String, Category> = emptyMap(),
    val isLoading: Boolean = true
) {
    val topCategories: List<CategoryBreakdownItem> get() = breakdown.take(3)
    val breakdownTotalMinor: Long get() = breakdown.sumOf { it.totalMinor }
}
