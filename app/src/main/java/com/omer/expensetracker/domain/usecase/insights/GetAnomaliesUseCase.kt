package com.omer.expensetracker.domain.usecase.insights

import com.omer.expensetracker.domain.model.AnomalyFlag
import com.omer.expensetracker.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetAnomaliesUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(start: LocalDate, end: LocalDate): Flow<List<AnomalyFlag>> =
        insightsRepository.observeAnomalies(start, end)
}
