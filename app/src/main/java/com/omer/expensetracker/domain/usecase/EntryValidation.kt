package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.util.UseCaseResult

internal object EntryValidation {
    fun validate(type: EntryType, amountMinor: Long, categoryId: String?): UseCaseResult<Unit> {
        if (amountMinor <= 0L) {
            return UseCaseResult.Failure("Amount must be greater than zero")
        }
        if (type == EntryType.EXPENSE && categoryId.isNullOrBlank()) {
            return UseCaseResult.Failure("Category is required for an expense")
        }
        return UseCaseResult.Success(Unit)
    }
}
