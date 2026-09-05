package com.omer.expensetracker.domain.usecase.insights

import com.omer.expensetracker.domain.model.InsightsPeriod
import com.omer.expensetracker.domain.model.TrendPoint
import com.omer.expensetracker.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetSpendTrendUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(period: InsightsPeriod, anchor: LocalDate = LocalDate.now()): Flow<List<TrendPoint>> =
        insightsRepository.observeSpendTrend(period, anchor)
}
