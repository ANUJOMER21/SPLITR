package com.omer.expensetracker.domain.repository

import com.omer.expensetracker.domain.model.GoalContribution
import com.omer.expensetracker.domain.model.SavingsGoal
import com.omer.expensetracker.domain.model.SavingsGoalDetail
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SavingsGoalRepository {
    fun observeGoals(): Flow<List<SavingsGoalDetail>>
    fun observeGoal(id: String): Flow<SavingsGoalDetail?>

    suspend fun addGoal(name: String, targetAmountMinor: Long, targetDate: LocalDate?): SavingsGoal
    suspend fun updateGoal(goal: SavingsGoal)
    suspend fun deleteGoal(id: String)
    suspend fun setCompleted(id: String, isCompleted: Boolean)

    suspend fun addContribution(goalId: String, amountMinor: Long, date: LocalDate): GoalContribution
    suspend fun updateContribution(contribution: GoalContribution)
    suspend fun deleteContribution(id: String)
}
