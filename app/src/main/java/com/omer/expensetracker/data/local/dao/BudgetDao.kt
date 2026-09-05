package com.omer.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

private const val PROGRESS_QUERY = """
    SELECT
        budgets.*,
        categories.id AS cat_id,
        categories.name AS cat_name,
        categories.iconKey AS cat_iconKey,
        categories.colorArgb AS cat_colorArgb,
        categories.isDefault AS cat_isDefault,
        categories.isActive AS cat_isActive,
        categories.createdAt AS cat_createdAt,
        categories.updatedAt AS cat_updatedAt,
        COALESCE(SUM(
            CASE
                WHEN entries.id IS NULL THEN 0
                WHEN budgets.categoryId IS NULL THEN entries.amountMinor
                WHEN entries.categoryId = budgets.categoryId THEN entries.amountMinor
                ELSE 0
            END
        ), 0) AS spentMinor
    FROM budgets
    LEFT JOIN entries ON entries.type = 'EXPENSE' AND entries.isDeleted = 0
        AND entries.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
    LEFT JOIN categories ON categories.id = budgets.categoryId
    WHERE budgets.isDeleted = 0
    GROUP BY budgets.id
    ORDER BY categories.name ASC
"""

@Dao
interface BudgetDao {

    @Query(PROGRESS_QUERY)
    fun observeBudgetsWithProgress(startEpochDay: Long, endEpochDay: Long): Flow<List<BudgetProgressRow>>

    @Query(PROGRESS_QUERY)
    suspend fun getBudgetsWithProgressSnapshot(startEpochDay: Long, endEpochDay: Long): List<BudgetProgressRow>

    @Query("SELECT * FROM budgets WHERE isDeleted = 0")
    suspend fun getAllActive(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE (categoryId = :categoryId OR (:categoryId IS NULL AND categoryId IS NULL)) AND isDeleted = 0 LIMIT 1")
    suspend fun getByCategoryId(categoryId: String?): BudgetEntity?

    @Insert
    suspend fun insert(budget: BudgetEntity)

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Insert
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("UPDATE budgets SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE budgets SET warningNotifiedMonth = :month, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setWarningNotifiedMonth(id: String, month: String, updatedAt: Long)

    @Query("UPDATE budgets SET breachNotifiedMonth = :month, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setBreachNotifiedMonth(id: String, month: String, updatedAt: Long)
}
