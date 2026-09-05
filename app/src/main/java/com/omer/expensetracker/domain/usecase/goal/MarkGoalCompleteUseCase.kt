package com.omer.expensetracker.domain.usecase.goal

import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import javax.inject.Inject

class MarkGoalCompleteUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(id: String, isCompleted: Boolean) = savingsGoalRepository.setCompleted(id, isCompleted)
}
