package com.omer.expensetracker.domain.usecase.goal

import com.omer.expensetracker.domain.model.SavingsGoalDetail
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGoalsUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    operator fun invoke(): Flow<List<SavingsGoalDetail>> = savingsGoalRepository.observeGoals()
}
