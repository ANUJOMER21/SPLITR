package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.OTHER_CATEGORY_ID
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class SetCategoryActiveUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: String, isActive: Boolean): UseCaseResult<Unit> {
        if (id == OTHER_CATEGORY_ID && !isActive) {
            return UseCaseResult.Failure("The Other category can't be hidden")
        }
        categoryRepository.setCategoryActive(id, isActive)
        return UseCaseResult.Success(Unit)
    }
}
