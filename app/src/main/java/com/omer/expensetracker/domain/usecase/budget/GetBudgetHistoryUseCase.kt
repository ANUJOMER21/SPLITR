package com.omer.expensetracker.domain.usecase.budget

import com.omer.expensetracker.domain.model.BudgetMonthSnapshot
import com.omer.expensetracker.domain.repository.BudgetRepository
import java.time.YearMonth
import javax.inject.Inject

class GetBudgetHistoryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(month: YearMonth): List<BudgetMonthSnapshot> =
        budgetRepository.getBudgetHistory(month)
}
