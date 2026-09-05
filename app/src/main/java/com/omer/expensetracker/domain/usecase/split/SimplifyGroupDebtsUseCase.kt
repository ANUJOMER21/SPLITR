package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Computes the minimum set of payments that would settle a group. Every balance this app
 * tracks is between one friend and the app owner (see [YOU_FRIEND_ID]), so the owner is added
 * to the net-balance set as the exact offsetting counterparty before running
 * [DebtSimplifier] — the result still always reduces every member (including "You") to zero.
 *
 * [DebtSimplifier] treats positive as "is owed money" (creditor). [Balance.netMinor] uses the
 * opposite sign — positive means the *friend* owes the owner, i.e. the friend is the debtor —
 * so every friend balance is negated before handing it to the simplifier, and the owner's own
 * balance is the exact negative of that sum (so the set nets to zero, by construction). */
class SimplifyGroupDebtsUseCase @Inject constructor(
    private val balanceRepository: BalanceRepository
) {
    suspend operator fun invoke(groupId: String): List<DebtSimplifier.Payment> {
        val balances = balanceRepository.observeGroupBalances(groupId).first()
        val net = DebtSimplifier.toCreditorDebtorMap(balances.associate { it.friendId to it.netMinor })
        return DebtSimplifier.simplify(net)
    }
}
