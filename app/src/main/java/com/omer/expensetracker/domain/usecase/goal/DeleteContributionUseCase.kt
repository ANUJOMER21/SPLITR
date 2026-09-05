package com.omer.expensetracker.domain.usecase.goal

import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import javax.inject.Inject

class DeleteContributionUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(id: String) = savingsGoalRepository.deleteContribution(id)
}
