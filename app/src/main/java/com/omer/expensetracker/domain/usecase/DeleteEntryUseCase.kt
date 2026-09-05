package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.repository.EntryRepository
import javax.inject.Inject

class DeleteEntryUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(id: String) = entryRepository.softDeleteEntry(id)
}
