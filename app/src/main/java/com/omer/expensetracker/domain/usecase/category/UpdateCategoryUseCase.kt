package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): UseCaseResult<Unit> {
        if (category.name.isBlank()) return UseCaseResult.Failure("Category name cannot be empty")
        categoryRepository.updateCategory(category.copy(updatedAt = System.currentTimeMillis()))
        return UseCaseResult.Success(Unit)
    }
}
