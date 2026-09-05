package com.omer.expensetracker.domain.usecase.goal

import com.omer.expensetracker.domain.model.GoalContribution
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class EditContributionUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(contribution: GoalContribution): UseCaseResult<Unit> {
        if (contribution.amountMinor <= 0L) return UseCaseResult.Failure("Amount must be greater than zero")
        savingsGoalRepository.updateContribution(contribution.copy(updatedAt = System.currentTimeMillis()))
        return UseCaseResult.Success(Unit)
    }
}
