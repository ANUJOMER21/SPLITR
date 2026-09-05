package com.omer.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {

    @Query("SELECT * FROM recurring_rules WHERE isDeleted = 0 ORDER BY nextDueDateEpochDay ASC")
    fun observeAll(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun getById(id: String): RecurringRuleEntity?

    @Insert
    suspend fun insert(rule: RecurringRuleEntity)

    @Upsert
    suspend fun upsert(rule: RecurringRuleEntity)

    @Insert
    suspend fun insertAll(rules: List<RecurringRuleEntity>)

    @Query("SELECT * FROM recurring_rules WHERE isDeleted = 0")
    suspend fun getAllActive(): List<RecurringRuleEntity>

    @Query("DELETE FROM recurring_rules")
    suspend fun deleteAll()

    @Update
    suspend fun update(rule: RecurringRuleEntity)

    @Query("UPDATE recurring_rules SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE recurring_rules SET isPaused = :isPaused, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPaused(id: String, isPaused: Boolean, updatedAt: Long)

    @Query("UPDATE recurring_rules SET skipNextOccurrence = :skip, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setSkipNextOccurrence(id: String, skip: Boolean, updatedAt: Long)

    @Query(
        """
        SELECT * FROM recurring_rules
        WHERE isDeleted = 0 AND isPaused = 0
            AND nextDueDateEpochDay <= :todayEpochDay
            AND (endDateEpochDay IS NULL OR nextDueDateEpochDay <= endDateEpochDay)
        """
    )
    suspend fun getDueRules(todayEpochDay: Long): List<RecurringRuleEntity>

    @Query(
        """
        UPDATE recurring_rules
        SET lastGeneratedDateEpochDay = :generatedDateEpochDay, nextDueDateEpochDay = :nextDueDateEpochDay, updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun recordGenerated(id: String, generatedDateEpochDay: Long, nextDueDateEpochDay: Long, updatedAt: Long)
}
