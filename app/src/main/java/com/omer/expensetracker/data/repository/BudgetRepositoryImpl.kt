package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.BudgetDao
import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.entity.BudgetEntity
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
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
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val syncOutbox: SyncOutbox
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
            syncOutbox.enqueue(SyncEntityType.BUDGET, updated.id, SyncOperation.UPSERT)
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
            syncOutbox.enqueue(SyncEntityType.BUDGET, budget.id, SyncOperation.UPSERT)
            budget
        }
    }

    override suspend fun deleteBudget(id: String) {
        budgetDao.softDelete(id, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.BUDGET, id, SyncOperation.DELETE)
    }

    override suspend fun upsertFromRemote(budget: Budget) {
        val existing = budgetDao.getById(budget.id)
        if (existing != null && existing.updatedAt >= budget.updatedAt) return
        val safeCategoryId = budget.categoryId?.takeIf { categoryDao.getById(it) != null }
        budgetDao.upsert(
            BudgetEntity(
                id = budget.id,
                categoryId = safeCategoryId,
                monthlyLimitMinor = budget.monthlyLimitMinor,
                warningNotifiedMonth = budget.warningNotifiedMonth,
                breachNotifiedMonth = budget.breachNotifiedMonth,
                createdAt = budget.createdAt,
                updatedAt = budget.updatedAt
            )
        )
    }

    override suspend fun deleteFromRemote(id: String) {
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
