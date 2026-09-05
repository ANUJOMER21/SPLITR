package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.GoalContributionDao
import com.omer.expensetracker.data.local.dao.SavingsGoalDao
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.mapper.toEntity
import com.omer.expensetracker.domain.model.GoalContribution
import com.omer.expensetracker.domain.model.SavingsGoal
import com.omer.expensetracker.domain.model.SavingsGoalDetail
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class SavingsGoalRepositoryImpl @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao,
    private val goalContributionDao: GoalContributionDao
) : SavingsGoalRepository {

    override fun observeGoals(): Flow<List<SavingsGoalDetail>> =
        savingsGoalDao.observeGoalsWithContributions().map { list -> list.map { it.toDomain() } }

    override fun observeGoal(id: String): Flow<SavingsGoalDetail?> =
        savingsGoalDao.observeGoalWithContributions(id).map { it?.toDomain() }

    override suspend fun addGoal(name: String, targetAmountMinor: Long, targetDate: LocalDate?): SavingsGoal {
        val now = System.currentTimeMillis()
        val goal = SavingsGoal(
            id = UUID.randomUUID().toString(),
            name = name,
            targetAmountMinor = targetAmountMinor,
            targetDate = targetDate,
            isCompleted = false,
            createdAt = now,
            updatedAt = now
        )
        savingsGoalDao.insert(goal.toEntity())
        return goal
    }

    override suspend fun updateGoal(goal: SavingsGoal) {
        savingsGoalDao.update(goal.toEntity())
    }

    override suspend fun deleteGoal(id: String) {
        savingsGoalDao.softDelete(id, System.currentTimeMillis())
    }

    override suspend fun setCompleted(id: String, isCompleted: Boolean) {
        savingsGoalDao.setCompleted(id, isCompleted, System.currentTimeMillis())
    }

    override suspend fun addContribution(goalId: String, amountMinor: Long, date: LocalDate): GoalContribution {
        val now = System.currentTimeMillis()
        val contribution = GoalContribution(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            amountMinor = amountMinor,
            date = date,
            createdAt = now,
            updatedAt = now
        )
        goalContributionDao.insert(contribution.toEntity())
        return contribution
    }

    override suspend fun updateContribution(contribution: GoalContribution) {
        goalContributionDao.update(contribution.toEntity())
    }

    override suspend fun deleteContribution(id: String) {
        goalContributionDao.softDelete(id, System.currentTimeMillis())
    }
}
