package com.omer.expensetracker.domain.usecase.goal

import com.omer.expensetracker.domain.model.SavingsGoal
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import java.time.LocalDate
import javax.inject.Inject

class AddSavingsGoalUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(name: String, targetAmountMinor: Long, targetDate: LocalDate?): UseCaseResult<SavingsGoal> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return UseCaseResult.Failure("Goal name cannot be empty")
        if (targetAmountMinor <= 0L) return UseCaseResult.Failure("Target amount must be greater than zero")
        if (targetDate != null && targetDate.isBefore(LocalDate.now())) {
            return UseCaseResult.Failure("Target date must be in the future")
        }
        return UseCaseResult.Success(savingsGoalRepository.addGoal(trimmed, targetAmountMinor, targetDate))
    }
}
