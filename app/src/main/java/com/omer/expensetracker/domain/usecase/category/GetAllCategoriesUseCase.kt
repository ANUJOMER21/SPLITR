package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.observeAllCategories()
}
