package com.omer.expensetracker.domain.usecase.insights

import com.omer.expensetracker.domain.model.HeatmapCell
import com.omer.expensetracker.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetHeatmapUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(start: LocalDate, end: LocalDate): Flow<List<HeatmapCell>> =
        insightsRepository.observeHeatmap(start, end)
}
