package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilteredEntriesUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    operator fun invoke(filter: EntryFilter): Flow<List<Entry>> =
        entryRepository.observeFilteredEntries(filter)
}
