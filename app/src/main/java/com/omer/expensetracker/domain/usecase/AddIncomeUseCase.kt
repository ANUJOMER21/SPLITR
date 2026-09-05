package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class AddIncomeUseCase @Inject constructor(
    private val entryRepository: EntryRepository
) {
    suspend operator fun invoke(amountMinor: Long, date: LocalDate, note: String? = null, photoUri: String? = null): UseCaseResult<Entry> {
        val validation = EntryValidation.validate(EntryType.INCOME, amountMinor, categoryId = null)
        if (validation is UseCaseResult.Failure) return validation

        val now = System.currentTimeMillis()
        val entry = Entry(
            id = UUID.randomUUID().toString(),
            type = EntryType.INCOME,
            amountMinor = amountMinor,
            categoryId = null,
            date = date,
            note = note,
            photoUri = photoUri,
            createdAt = now,
            updatedAt = now
        )
        return UseCaseResult.Success(entryRepository.addEntry(entry))
    }
}
