package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID

/**
 * Pure, independently testable debt-simplification: given each person's net balance, computes
 * the minimum set of payments that would fully settle everyone. No database or UI dependency.
 */
object DebtSimplifier {

    data class Payment(val fromFriendId: String, val toFriendId: String, val amountMinor: Long)

    /** Greedy minimum-cash-flow simplification. [netBalances]: positive means owed money
     * (creditor), negative means owes money (debtor), zero entries are ignored. Repeatedly
     * matches the largest creditor with the largest debtor — each step fully settles at least
     * one person, so the result never has more transactions than the raw pairwise debt list,
     * and settling every returned payment brings every balance to exactly zero. */
    fun simplify(netBalances: Map<String, Long>): List<Payment> {
        val balances = netBalances.filterValues { it != 0L }.toMutableMap()
        val payments = mutableListOf<Payment>()

        while (true) {
            val creditor = balances.maxByOrNull { it.value } ?: break
            val debtor = balances.minByOrNull { it.value } ?: break
            if (creditor.value <= 0L || debtor.value >= 0L) break

            val amount = minOf(creditor.value, -debtor.value)
            payments += Payment(fromFriendId = debtor.key, toFriendId = creditor.key, amountMinor = amount)

            val newCreditorBalance = creditor.value - amount
            val newDebtorBalance = debtor.value + amount
            if (newCreditorBalance == 0L) balances.remove(creditor.key) else balances[creditor.key] = newCreditorBalance
            if (newDebtorBalance == 0L) balances.remove(debtor.key) else balances[debtor.key] = newDebtorBalance
        }
        return payments
    }

    /** Converts [Balance][com.omer.expensetracker.domain.model.split.Balance]-style balances
     * (keyed by friend id, positive = *the friend* owes the owner) into the creditor/debtor
     * convention [simplify] expects (positive = *is owed* money), adding an entry for
     * [YOU_FRIEND_ID] so the whole set nets to exactly zero. Every friend entry is simply
     * negated; the owner's entry is the exact negative of that sum, by construction. */
    fun toCreditorDebtorMap(friendNetMinorOwedToYou: Map<String, Long>): Map<String, Long> {
        val net = friendNetMinorOwedToYou.mapValues { (_, netMinor) -> -netMinor }.toMutableMap()
        net[YOU_FRIEND_ID] = -net.values.sum()
        return net
    }
}
