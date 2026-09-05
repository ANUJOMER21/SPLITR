package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(name: String, iconKey: String, colorArgb: Long): UseCaseResult<Category> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return UseCaseResult.Failure("Category name cannot be empty")
        return UseCaseResult.Success(categoryRepository.addCategory(trimmed, iconKey, colorArgb))
    }
}
