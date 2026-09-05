package com.omer.expensetracker.domain.usecase.insights

import com.omer.expensetracker.domain.model.InsightsPeriod
import com.omer.expensetracker.domain.model.SavingsRatePoint
import com.omer.expensetracker.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetSavingsRateTrendUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(period: InsightsPeriod, anchor: LocalDate = LocalDate.now()): Flow<List<SavingsRatePoint>> =
        insightsRepository.observeSavingsRateTrend(period, anchor)
}
