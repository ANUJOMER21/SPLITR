package com.omer.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightsDao {
    /** Raw (type, amount, category, date) rows in range — every insight (trend, top
     * categories, heatmap, month-over-month, savings rate, anomalies) is derived from this
     * single reactive query in Kotlin rather than one bespoke aggregate SQL query per insight. */
    @Query(
        """
        SELECT id, type, amountMinor, categoryId, dateEpochDay FROM entries
        WHERE isDeleted = 0 AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY dateEpochDay ASC
        """
    )
    fun observeEntriesInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<InsightsRawRow>>
}
