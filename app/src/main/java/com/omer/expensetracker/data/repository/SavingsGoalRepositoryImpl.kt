package com.omer.expensetracker.data.repository

import androidx.room.withTransaction
import com.omer.expensetracker.data.local.AppDatabase
import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.local.dao.GoalContributionDao
import com.omer.expensetracker.data.local.dao.SavingsGoalDao
import com.omer.expensetracker.data.local.entity.CategoryEntity
import com.omer.expensetracker.data.local.entity.EntryEntity
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.mapper.toEntity
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
import com.omer.expensetracker.domain.model.GoalContribution
import com.omer.expensetracker.domain.model.SAVINGS_CATEGORY_ID
import com.omer.expensetracker.domain.model.SavingsGoal
import com.omer.expensetracker.domain.model.SavingsGoalDetail
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class SavingsGoalRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val savingsGoalDao: SavingsGoalDao,
    private val goalContributionDao: GoalContributionDao,
    private val entryDao: EntryDao,
    private val categoryDao: CategoryDao,
    private val syncOutbox: SyncOutbox
) : SavingsGoalRepository {

    private suspend fun enqueueGoal(goalId: String) =
        syncOutbox.enqueue(SyncEntityType.GOAL, goalId, SyncOperation.UPSERT)

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
        enqueueGoal(goal.id)
        return goal
    }

    override suspend fun updateGoal(goal: SavingsGoal) {
        savingsGoalDao.update(goal.toEntity())
        enqueueGoal(goal.id)
    }

    override suspend fun deleteGoal(id: String) {
        savingsGoalDao.softDelete(id, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.GOAL, id, SyncOperation.DELETE)
    }

    override suspend fun setCompleted(id: String, isCompleted: Boolean) {
        savingsGoalDao.setCompleted(id, isCompleted, System.currentTimeMillis())
        enqueueGoal(id)
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
        db.withTransaction {
            goalContributionDao.insert(contribution.toEntity())
            syncLinkedEntry(contribution.id, amountMinor, date, deleted = false)
        }
        enqueueGoal(goalId)
        return contribution
    }

    override suspend fun updateContribution(contribution: GoalContribution) {
        db.withTransaction {
            goalContributionDao.update(contribution.toEntity())
            syncLinkedEntry(contribution.id, contribution.amountMinor, contribution.date, deleted = false)
        }
        enqueueGoal(contribution.goalId)
    }

    override suspend fun deleteContribution(id: String) {
        val goalId = goalContributionDao.getById(id)?.goalId
        db.withTransaction {
            goalContributionDao.softDelete(id, System.currentTimeMillis())
            syncLinkedEntry(id, 0L, LocalDate.now(), deleted = true)
        }
        if (goalId != null) enqueueGoal(goalId)
    }

    override suspend fun upsertFromRemote(detail: SavingsGoalDetail) {
        db.withTransaction {
            val now = System.currentTimeMillis()
            val localGoal = savingsGoalDao.getById(detail.goal.id)
            if (localGoal == null || localGoal.updatedAt < detail.goal.updatedAt) {
                savingsGoalDao.upsert(detail.goal.toEntity())
            }
            val remoteIds = detail.contributions.map { it.id }.toSet()
            goalContributionDao.getForGoal(detail.goal.id)
                .filter { it.id !in remoteIds }
                .forEach {
                    goalContributionDao.softDelete(it.id, now)
                    syncLinkedEntry(it.id, 0L, LocalDate.now(), deleted = true)
                }
            detail.contributions.forEach { c ->
                goalContributionDao.upsert(c.toEntity())
                syncLinkedEntry(c.id, c.amountMinor, c.date, deleted = false)
            }
        }
    }

    override suspend fun deleteFromRemote(id: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            savingsGoalDao.softDelete(id, now)
            goalContributionDao.getForGoal(id).forEach {
                goalContributionDao.softDelete(it.id, now)
                syncLinkedEntry(it.id, 0L, LocalDate.now(), deleted = true)
            }
        }
    }

    /** Mirrors a contribution into a personal EXPENSE entry under the "Savings" category, so
     * money moved into a goal still counts toward that day's spending and the dashboard totals —
     * the same link/keep-in-sync approach [com.omer.expensetracker.data.repository.split
     * .SharedExpenseRepositoryImpl] uses for your share of a shared expense. */
    private suspend fun syncLinkedEntry(contributionId: String, amountMinor: Long, date: LocalDate, deleted: Boolean) {
        val now = System.currentTimeMillis()
        val existing = entryDao.getByLinkedGoalContributionId(contributionId)
        when {
            deleted -> existing?.let { entryDao.softDelete(it.id, now) }
            existing != null -> entryDao.update(
                existing.copy(amountMinor = amountMinor, dateEpochDay = date.toEpochDay(), updatedAt = now)
            )
            else -> {
                ensureSavingsCategory(now)
                entryDao.insert(
                    EntryEntity(
                        id = UUID.randomUUID().toString(), type = "EXPENSE", amountMinor = amountMinor,
                        categoryId = SAVINGS_CATEGORY_ID, dateEpochDay = date.toEpochDay(),
                        createdAt = now, updatedAt = now, linkedGoalContributionId = contributionId
                    )
                )
            }
        }
    }

    private suspend fun ensureSavingsCategory(now: Long) {
        if (categoryDao.getById(SAVINGS_CATEGORY_ID) != null) return
        categoryDao.insert(
            CategoryEntity(
                id = SAVINGS_CATEGORY_ID, name = "Savings", iconKey = "savings", colorArgb = 0xFF26C6DAL,
                isDefault = true, isActive = true, createdAt = now, updatedAt = now
            )
        )
    }
}
