package com.omer.expensetracker.domain.usecase.budget

import com.omer.expensetracker.domain.model.BudgetProgress
import com.omer.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetBudgetsWithProgressUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(month: YearMonth): Flow<List<BudgetProgress>> =
        budgetRepository.observeBudgetsWithProgress(month)
}
