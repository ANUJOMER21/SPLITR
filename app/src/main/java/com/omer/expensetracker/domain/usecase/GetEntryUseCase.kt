package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.repository.EntryRepository
import javax.inject.Inject

class GetEntryUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(id: String): Entry? = entryRepository.getEntry(id)
}
