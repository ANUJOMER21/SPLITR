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

    /** Records a settlement split across several buckets — [allocations] maps a groupId (or
     * `null` for the non-group bucket) to that slice's amount. */
    suspend fun allocated(
        payerFriendId: String,
        receiverFriendId: String,
        date: LocalDate,
        note: String?,
        allocations: Map<String?, Long>
    ): UseCaseResult<List<Settlement>> {
        val slices = allocations.filterValues { it > 0L }
        if (slices.isEmpty()) return UseCaseResult.Failure("Amount must be greater than zero")
        if (payerFriendId == receiverFriendId) return UseCaseResult.Failure("Payer and receiver must be different people")
        return UseCaseResult.Success(
            settlementRepository.recordAllocatedSettlement(payerFriendId, receiverFriendId, date, note?.trim()?.ifBlank { null }, slices)
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
    /** Deletes the whole payment when [id] is one slice of a multi-group settlement batch. */
    suspend operator fun invoke(id: String) {
        val batchId = settlementRepository.getSettlement(id)?.batchId
        if (batchId != null) settlementRepository.deleteSettlementBatch(batchId)
        else settlementRepository.deleteSettlement(id)
    }
}
