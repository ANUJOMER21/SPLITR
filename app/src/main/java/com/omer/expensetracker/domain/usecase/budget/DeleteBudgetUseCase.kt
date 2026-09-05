package com.omer.expensetracker.domain.usecase.budget

import com.omer.expensetracker.domain.repository.BudgetRepository
import javax.inject.Inject

/** Deleting a budget only removes the limit — the underlying expense entries it was tracking
 * are never touched. */
class DeleteBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(id: String) = budgetRepository.deleteBudget(id)
}
