package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.CategoryWithTotal
import com.omer.expensetracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesWithTotalsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<CategoryWithTotal>> =
        categoryRepository.observeCategoriesWithCurrentMonthTotal()
}
