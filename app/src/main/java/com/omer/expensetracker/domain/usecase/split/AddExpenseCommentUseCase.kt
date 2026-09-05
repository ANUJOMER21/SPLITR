package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class AddExpenseCommentUseCase @Inject constructor(
    private val sharedExpenseRepository: SharedExpenseRepository
) {
    suspend operator fun invoke(expenseId: String, text: String): UseCaseResult<Unit> {
        if (text.isBlank()) return UseCaseResult.Failure("Comment can't be empty")
        sharedExpenseRepository.addComment(expenseId, text.trim())
        return UseCaseResult.Success(Unit)
    }
}
