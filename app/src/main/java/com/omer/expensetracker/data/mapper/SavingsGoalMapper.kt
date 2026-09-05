package com.omer.expensetracker.data.mapper

import com.omer.expensetracker.data.local.dao.GoalWithContributions
import com.omer.expensetracker.data.local.entity.GoalContributionEntity
import com.omer.expensetracker.data.local.entity.SavingsGoalEntity
import com.omer.expensetracker.domain.model.GoalContribution
import com.omer.expensetracker.domain.model.SavingsGoal
import com.omer.expensetracker.domain.model.SavingsGoalDetail
import java.time.LocalDate

fun SavingsGoalEntity.toDomain(): SavingsGoal = SavingsGoal(
    id = id,
    name = name,
    targetAmountMinor = targetAmountMinor,
    targetDate = targetDateEpochDay?.let { LocalDate.ofEpochDay(it) },
    isCompleted = isCompleted,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SavingsGoal.toEntity(): SavingsGoalEntity = SavingsGoalEntity(
    id = id,
    name = name,
    targetAmountMinor = targetAmountMinor,
    targetDateEpochDay = targetDate?.toEpochDay(),
    isCompleted = isCompleted,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun GoalContributionEntity.toDomain(): GoalContribution = GoalContribution(
    id = id,
    goalId = goalId,
    amountMinor = amountMinor,
    date = LocalDate.ofEpochDay(dateEpochDay),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun GoalContribution.toEntity(): GoalContributionEntity = GoalContributionEntity(
    id = id,
    goalId = goalId,
    amountMinor = amountMinor,
    dateEpochDay = date.toEpochDay(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun GoalWithContributions.toDomain(): SavingsGoalDetail = SavingsGoalDetail(
    goal = goal.toDomain(),
    contributions = activeContributions.map { it.toDomain() }.sortedByDescending { it.date }
)
