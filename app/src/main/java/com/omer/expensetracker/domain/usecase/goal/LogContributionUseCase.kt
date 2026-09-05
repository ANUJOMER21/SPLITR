package com.omer.expensetracker.domain.usecase.goal

import com.omer.expensetracker.domain.model.GoalContribution
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class LogContributionUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(goalId: String, amountMinor: Long, date: LocalDate): UseCaseResult<GoalContribution> {
        if (amountMinor <= 0L) return UseCaseResult.Failure("Amount must be greater than zero")
        val contribution = savingsGoalRepository.addContribution(goalId, amountMinor, date)

        val detail = savingsGoalRepository.observeGoal(goalId).first()
        if (detail != null && !detail.goal.isCompleted && detail.savedMinor >= detail.goal.targetAmountMinor) {
            savingsGoalRepository.setCompleted(goalId, true)
        }
        return UseCaseResult.Success(contribution)
    }
}
