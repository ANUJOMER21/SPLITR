package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.BudgetDao
import com.omer.expensetracker.data.local.entity.BudgetEntity
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.domain.model.Budget
import com.omer.expensetracker.domain.model.BudgetMonthSnapshot
import com.omer.expensetracker.domain.model.BudgetProgress
import com.omer.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun observeBudgetsWithProgress(month: YearMonth): Flow<List<BudgetProgress>> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return budgetDao.observeBudgetsWithProgress(start, end).map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun getBudgetsWithProgressSnapshot(month: YearMonth): List<BudgetProgress> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return budgetDao.getBudgetsWithProgressSnapshot(start, end).map { it.toDomain() }
    }

    /** History reflects the currently configured limit applied retroactively to past months'
     * actual spend — this build doesn't keep a change-log of past limit values. */
    override suspend fun getBudgetHistory(month: YearMonth): List<BudgetMonthSnapshot> =
        getBudgetsWithProgressSnapshot(month).map { progress ->
            BudgetMonthSnapshot(
                budget = progress.budget,
                category = progress.category,
                limitMinorThatMonth = progress.limitMinor,
                spentMinor = progress.spentMinor
            )
        }

    override suspend fun getBudget(id: String): Budget? = budgetDao.getById(id)?.toDomain()

    override suspend fun getBudgetForCategory(categoryId: String?): Budget? =
        budgetDao.getByCategoryId(categoryId)?.toDomain()

    override suspend fun upsertBudget(categoryId: String?, monthlyLimitMinor: Long): Budget {
        val now = System.currentTimeMillis()
        val existing = budgetDao.getByCategoryId(categoryId)
        return if (existing != null) {
            val updated = existing.copy(monthlyLimitMinor = monthlyLimitMinor, updatedAt = now)
            budgetDao.update(updated)
            updated.toDomain()
        } else {
            val budget = Budget(
                id = UUID.randomUUID().toString(),
                categoryId = categoryId,
                monthlyLimitMinor = monthlyLimitMinor,
                warningNotifiedMonth = null,
                breachNotifiedMonth = null,
                createdAt = now,
                updatedAt = now
            )
            budgetDao.insert(
                BudgetEntity(
                    id = budget.id,
                    categoryId = budget.categoryId,
                    monthlyLimitMinor = budget.monthlyLimitMinor,
                    warningNotifiedMonth = null,
                    breachNotifiedMonth = null,
                    createdAt = now,
                    updatedAt = now
                )
            )
            budget
        }
    }

    override suspend fun deleteBudget(id: String) {
        budgetDao.softDelete(id, System.currentTimeMillis())
    }

    override suspend fun markWarningNotified(id: String, yearMonth: String) {
        budgetDao.setWarningNotifiedMonth(id, yearMonth, System.currentTimeMillis())
    }

    override suspend fun markBreachNotified(id: String, yearMonth: String) {
        budgetDao.setBreachNotifiedMonth(id, yearMonth, System.currentTimeMillis())
    }

    override suspend fun getAllBudgets(): List<Budget> = budgetDao.getAllActive().map { it.toDomain() }
}
