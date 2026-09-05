package com.omer.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.omer.expensetracker.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @RawQuery(observedEntities = [EntryEntity::class])
    fun observeFiltered(query: SupportSQLiteQuery): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE linkedSharedExpenseId = :sharedExpenseId AND isDeleted = 0")
    suspend fun getByLinkedSharedExpenseId(sharedExpenseId: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE isDeleted = 0")
    suspend fun getAllActive(): List<EntryEntity>

    @Query("DELETE FROM entries")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(entry: EntryEntity)

    @Insert
    suspend fun insertAll(entries: List<EntryEntity>)

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("UPDATE entries SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE entries SET isDeleted = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restore(id: String, updatedAt: Long)

    @Query(
        """
        SELECT type, COALESCE(SUM(amountMinor), 0) AS total
        FROM entries
        WHERE isDeleted = 0 AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY type
        """
    )
    fun observeMonthlyTotals(startEpochDay: Long, endEpochDay: Long): Flow<List<TypeTotalRow>>

    @Query(
        """
        SELECT categories.*, SUM(entries.amountMinor) AS totalMinor
        FROM entries
        JOIN categories ON categories.id = entries.categoryId
        WHERE entries.type = 'EXPENSE'
            AND entries.isDeleted = 0
            AND entries.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY categories.id
        ORDER BY totalMinor DESC
        """
    )
    fun observeCategoryBreakdown(startEpochDay: Long, endEpochDay: Long): Flow<List<CategoryTotalRow>>

    @Query("UPDATE entries SET categoryId = :fallbackCategoryId, updatedAt = :updatedAt WHERE categoryId = :categoryId")
    suspend fun reassignCategory(categoryId: String, fallbackCategoryId: String, updatedAt: Long)
}
