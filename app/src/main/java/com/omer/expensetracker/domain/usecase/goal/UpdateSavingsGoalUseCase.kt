package com.omer.expensetracker.domain.usecase.goal

import com.omer.expensetracker.domain.model.SavingsGoal
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class UpdateSavingsGoalUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(goal: SavingsGoal): UseCaseResult<Unit> {
        if (goal.name.isBlank()) return UseCaseResult.Failure("Goal name cannot be empty")
        if (goal.targetAmountMinor <= 0L) return UseCaseResult.Failure("Target amount must be greater than zero")
        savingsGoalRepository.updateGoal(goal.copy(updatedAt = System.currentTimeMillis()))
        return UseCaseResult.Success(Unit)
    }
}
