package com.omer.expensetracker.domain.usecase.insights

import com.omer.expensetracker.domain.model.TopCategoryInsight
import com.omer.expensetracker.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTopCategoriesInsightUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(start: LocalDate, end: LocalDate): Flow<List<TopCategoryInsight>> =
        insightsRepository.observeTopCategories(start, end)
}
