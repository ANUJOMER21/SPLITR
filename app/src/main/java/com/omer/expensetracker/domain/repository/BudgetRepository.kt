package com.omer.expensetracker.domain.repository

import com.omer.expensetracker.domain.model.Budget
import com.omer.expensetracker.domain.model.BudgetMonthSnapshot
import com.omer.expensetracker.domain.model.BudgetProgress
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    fun observeBudgetsWithProgress(month: YearMonth): Flow<List<BudgetProgress>>
    suspend fun getBudgetsWithProgressSnapshot(month: YearMonth): List<BudgetProgress>
    suspend fun getBudgetHistory(month: YearMonth): List<BudgetMonthSnapshot>
    suspend fun getBudget(id: String): Budget?
    suspend fun getBudgetForCategory(categoryId: String?): Budget?
    suspend fun upsertBudget(categoryId: String?, monthlyLimitMinor: Long): Budget
    suspend fun deleteBudget(id: String)
    suspend fun markWarningNotified(id: String, yearMonth: String)
    suspend fun markBreachNotified(id: String, yearMonth: String)

    /** All active (non-deleted) budgets, for the background threshold-check job. */
    suspend fun getAllBudgets(): List<Budget>
}
