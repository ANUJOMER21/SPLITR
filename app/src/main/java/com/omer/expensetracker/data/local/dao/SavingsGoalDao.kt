package com.omer.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.omer.expensetracker.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {

    @Transaction
    @Query("SELECT * FROM savings_goals WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun observeGoalsWithContributions(): Flow<List<GoalWithContributions>>

    @Transaction
    @Query("SELECT * FROM savings_goals WHERE id = :id AND isDeleted = 0")
    fun observeGoalWithContributions(id: String): Flow<GoalWithContributions?>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getById(id: String): SavingsGoalEntity?

    @Insert
    suspend fun insert(goal: SavingsGoalEntity)

    @Upsert
    suspend fun upsert(goal: SavingsGoalEntity)

    @Insert
    suspend fun insertAll(goals: List<SavingsGoalEntity>)

    @Query("SELECT * FROM savings_goals WHERE isDeleted = 0")
    suspend fun getAllActive(): List<SavingsGoalEntity>

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAll()

    @Update
    suspend fun update(goal: SavingsGoalEntity)

    @Query("UPDATE savings_goals SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE savings_goals SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setCompleted(id: String, isCompleted: Boolean, updatedAt: Long)
}
