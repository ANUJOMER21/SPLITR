package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.MonthlySummary
import com.omer.expensetracker.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetMonthlySummaryUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    operator fun invoke(month: YearMonth): Flow<MonthlySummary> =
        entryRepository.observeMonthlySummary(month)
}
