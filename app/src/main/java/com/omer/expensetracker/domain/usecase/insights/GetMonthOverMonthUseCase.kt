package com.omer.expensetracker.domain.usecase.insights

import com.omer.expensetracker.domain.model.MonthOverMonthChange
import com.omer.expensetracker.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetMonthOverMonthByCategoryUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(currentMonthStart: LocalDate): Flow<List<MonthOverMonthChange>> =
        insightsRepository.observeMonthOverMonthByCategory(currentMonthStart)
}

class GetOverallMonthOverMonthUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(currentMonthStart: LocalDate): Flow<MonthOverMonthChange> =
        insightsRepository.observeOverallMonthOverMonth(currentMonthStart)
}
