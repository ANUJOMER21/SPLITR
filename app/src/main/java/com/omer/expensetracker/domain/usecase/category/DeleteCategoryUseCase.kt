package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.OTHER_CATEGORY_ID
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

/**
 * Deletion policy: only custom (non-default) categories can be deleted. Deleting one
 * reassigns every entry that references it to the "Other" category, then deactivates it —
 * no entry is ever left pointing at a nonexistent category. Default categories can only be
 * hidden via [SetCategoryActiveUseCase], never deleted, so historical entries stay meaningful.
 */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: String): UseCaseResult<Unit> {
        if (id == OTHER_CATEGORY_ID) {
            return UseCaseResult.Failure("The Other category can't be deleted")
        }
        val category = categoryRepository.getCategory(id)
            ?: return UseCaseResult.Failure("Category not found")
        if (category.isDefault) {
            return UseCaseResult.Failure("Default categories can only be hidden, not deleted")
        }
        categoryRepository.deleteCustomCategory(id)
        return UseCaseResult.Success(Unit)
    }
}
