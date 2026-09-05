package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.CategoryBreakdownItem
import com.omer.expensetracker.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetCategoryBreakdownUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    operator fun invoke(month: YearMonth): Flow<List<CategoryBreakdownItem>> =
        entryRepository.observeCategoryBreakdown(month)
}
