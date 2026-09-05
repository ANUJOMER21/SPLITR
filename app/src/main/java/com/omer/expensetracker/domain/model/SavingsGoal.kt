package com.omer.expensetracker.domain.model

import java.time.LocalDate

data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmountMinor: Long,
    val targetDate: LocalDate?,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class GoalContribution(
    val id: String,
    val goalId: String,
    val amountMinor: Long,
    val date: LocalDate,
    val createdAt: Long,
    val updatedAt: Long
)

data class SavingsGoalDetail(
    val goal: SavingsGoal,
    val contributions: List<GoalContribution>
) {
    val savedMinor: Long get() = contributions.sumOf { it.amountMinor }
    val remainingMinor: Long get() = (goal.targetAmountMinor - savedMinor).coerceAtLeast(0)
    val fraction: Float get() = if (goal.targetAmountMinor > 0) savedMinor.toFloat() / goal.targetAmountMinor else 0f

    /** Null when there's no target date, or not enough data (no contributions yet) to project a pace. */
    val isOnTrack: Boolean?
        get() {
            val target = goal.targetDate ?: return null
            val first = contributions.minByOrNull { it.date }?.date ?: return null
            val today = LocalDate.now()
            if (today.isBefore(first) || savedMinor <= 0) return null
            val daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(first, today).coerceAtLeast(1)
            val daysTotal = java.time.temporal.ChronoUnit.DAYS.between(first, target).coerceAtLeast(1)
            val requiredPacePerDay = goal.targetAmountMinor.toDouble() / daysTotal
            val actualPacePerDay = savedMinor.toDouble() / daysElapsed
            return actualPacePerDay >= requiredPacePerDay
        }
}
