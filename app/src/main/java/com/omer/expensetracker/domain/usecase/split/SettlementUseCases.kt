package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.model.split.Settlement
import com.omer.expensetracker.domain.repository.split.SettlementRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import java.time.LocalDate
import javax.inject.Inject

class RecordSettlementUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(
        payerFriendId: String,
        receiverFriendId: String,
        amountMinor: Long,
        date: LocalDate,
        note: String?,
        groupId: String?
    ): UseCaseResult<Settlement> {
        if (amountMinor <= 0L) return UseCaseResult.Failure("Amount must be greater than zero")
        if (payerFriendId == receiverFriendId) return UseCaseResult.Failure("Payer and receiver must be different people")
        return UseCaseResult.Success(
            settlementRepository.recordSettlement(payerFriendId, receiverFriendId, amountMinor, date, note?.trim()?.ifBlank { null }, groupId)
        )
    }
}

class EditSettlementUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(id: String, amountMinor: Long, date: LocalDate, note: String?): UseCaseResult<Unit> {
        if (amountMinor <= 0L) return UseCaseResult.Failure("Amount must be greater than zero")
        settlementRepository.editSettlement(id, amountMinor, date, note?.trim()?.ifBlank { null })
        return UseCaseResult.Success(Unit)
    }
}

class DeleteSettlementUseCase @Inject constructor(
    private val settlementRepository: SettlementRepository
) {
    suspend operator fun invoke(id: String) = settlementRepository.deleteSettlement(id)
}
