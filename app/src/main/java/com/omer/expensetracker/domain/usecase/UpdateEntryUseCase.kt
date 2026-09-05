package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class UpdateEntryUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(entry: Entry): UseCaseResult<Unit> {
        val validation = EntryValidation.validate(entry.type, entry.amountMinor, entry.categoryId)
        if (validation is UseCaseResult.Failure) return validation

        entryRepository.updateEntry(entry.copy(updatedAt = System.currentTimeMillis()))
        return UseCaseResult.Success(Unit)
    }
}
