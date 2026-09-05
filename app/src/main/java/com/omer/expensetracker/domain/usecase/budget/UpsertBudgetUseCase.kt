package com.omer.expensetracker.domain.usecase.budget

import com.omer.expensetracker.domain.model.Budget
import com.omer.expensetracker.domain.repository.BudgetRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class UpsertBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(categoryId: String?, monthlyLimitMinor: Long): UseCaseResult<Budget> {
        if (monthlyLimitMinor <= 0L) {
            return UseCaseResult.Failure("Budget limit must be greater than zero")
        }
        return UseCaseResult.Success(budgetRepository.upsertBudget(categoryId, monthlyLimitMinor))
    }
}
