package com.omer.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.GoalContributionEntity

@Dao
interface GoalContributionDao {

    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId AND isDeleted = 0 ORDER BY dateEpochDay DESC")
    suspend fun getForGoal(goalId: String): List<GoalContributionEntity>

    @Insert
    suspend fun insert(contribution: GoalContributionEntity)

    @Insert
    suspend fun insertAll(contributions: List<GoalContributionEntity>)

    @Query("SELECT * FROM goal_contributions WHERE isDeleted = 0")
    suspend fun getAllActive(): List<GoalContributionEntity>

    @Query("DELETE FROM goal_contributions")
    suspend fun deleteAll()

    @Update
    suspend fun update(contribution: GoalContributionEntity)

    @Query("UPDATE goal_contributions SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)
}
