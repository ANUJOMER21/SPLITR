package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: String): Category? = categoryRepository.getCategory(id)
}
