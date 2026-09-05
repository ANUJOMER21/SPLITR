package com.omer.expensetracker.domain.usecase.budget

import com.omer.expensetracker.domain.model.Budget
import com.omer.expensetracker.domain.repository.BudgetRepository
import javax.inject.Inject

class GetBudgetForCategoryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(categoryId: String?): Budget? = budgetRepository.getBudgetForCategory(categoryId)
}
