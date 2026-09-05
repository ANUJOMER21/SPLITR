package com.omer.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY isDefault DESC, name ASC")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT categories.*,
            COALESCE(SUM(
                CASE WHEN entries.type = 'EXPENSE'
                    AND entries.isDeleted = 0
                    AND entries.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
                THEN entries.amountMinor ELSE 0 END
            ), 0) AS totalMinor
        FROM categories
        LEFT JOIN entries ON entries.categoryId = categories.id
        GROUP BY categories.id
        ORDER BY categories.isDefault DESC, categories.name ASC
        """
    )
    fun observeCategoriesWithMonthTotal(startEpochDay: Long, endEpochDay: Long): Flow<List<CategoryTotalRow>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories")
    suspend fun getAllSync(): List<CategoryEntity>

    @Insert
    suspend fun insert(category: CategoryEntity)

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setActive(id: String, isActive: Boolean, updatedAt: Long)
}
