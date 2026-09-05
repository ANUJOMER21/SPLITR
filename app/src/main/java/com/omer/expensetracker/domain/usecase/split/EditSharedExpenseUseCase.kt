package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.repository.split.ExpenseWrite
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import java.time.LocalDate
import javax.inject.Inject

class EditSharedExpenseUseCase @Inject constructor(
    private val sharedExpenseRepository: SharedExpenseRepository
) {
    suspend operator fun invoke(
        id: String,
        description: String,
        amountMinor: Long,
        categoryId: String?,
        date: LocalDate,
        groupId: String?,
        photoUri: String?,
        paidByFriendId: Map<String, Long>,
        split: SplitInput
    ): UseCaseResult<Unit> {
        if (description.isBlank()) return UseCaseResult.Failure("Enter a description")
        if (amountMinor <= 0L) return UseCaseResult.Failure("Amount must be greater than zero")
        if (paidByFriendId.isEmpty()) return UseCaseResult.Failure("At least one payer is required")
        if (paidByFriendId.values.sum() != amountMinor) return UseCaseResult.Failure("Payer amounts must sum to the total")

        val (owedByFriendId, splitType) = try {
            computeOwedShares(amountMinor, split)
        } catch (e: SplitCalculator.SplitException) {
            return UseCaseResult.Failure(e.message ?: "Invalid split")
        } catch (e: IllegalArgumentException) {
            return UseCaseResult.Failure(e.message ?: "Invalid split")
        }

        sharedExpenseRepository.editExpense(
            id,
            ExpenseWrite(
                description = description.trim(),
                amountMinor = amountMinor,
                categoryId = categoryId,
                splitType = splitType,
                date = date,
                groupId = groupId,
                photoUri = photoUri,
                paidByFriendId = paidByFriendId,
                owedByFriendId = owedByFriendId
            )
        )
        return UseCaseResult.Success(Unit)
    }
}
