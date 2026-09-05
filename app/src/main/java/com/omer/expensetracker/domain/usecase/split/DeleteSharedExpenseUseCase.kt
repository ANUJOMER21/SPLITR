package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import javax.inject.Inject

class DeleteSharedExpenseUseCase @Inject constructor(
    private val sharedExpenseRepository: SharedExpenseRepository
) {
    suspend operator fun invoke(id: String) = sharedExpenseRepository.deleteExpense(id)
}
