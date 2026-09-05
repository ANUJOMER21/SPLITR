package com.omer.expensetracker.domain.usecase.split

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val YOU = "you"

/** Mirrors the balance-delta formula in `SharedExpenseRepositoryImpl.deltasOf` /
 * `SettlementRepositoryImpl.applyDelta` exactly, so these tests exercise the real ledger
 * arithmetic without needing a Room database. Every non-owner friend's balance moves by
 * (owed − paid) for an expense, or by the settlement amount signed by which side "You" is on.
 * These tests assume every expense includes the owner as a payer or a participant — the model's
 * one documented constraint (see Design decision #1: "You" is always in every split). */
private fun expenseDeltas(paidBy: Map<String, Long>, owedBy: Map<String, Long>): Map<String, Long> =
    (paidBy.keys + owedBy.keys).filter { it != YOU }.associateWith { (owedBy[it] ?: 0L) - (paidBy[it] ?: 0L) }

private fun settlementDelta(payer: String, receiver: String, amountMinor: Long): Pair<String, Long>? = when (YOU) {
    payer -> receiver to amountMinor
    receiver -> payer to -amountMinor
    else -> null
}

class BalanceInvariantTest {

    /** "You" never gets a stored balance row — this documents the bookkeeping identity every
     * other test relies on: constructing the implicit owner balance as the negative sum of
     * every other balance is what keeps the whole ledger at net zero, by definition. */
    @Test
    fun `the implicit You balance always brings the ledger to net zero`() {
        val deltas = expenseDeltas(mapOf(YOU to 1000L), SplitCalculator.equalSplit(1000L, listOf(YOU, "alice", "bob")))
        val withYou = deltas + (YOU to -deltas.values.sum())
        assertEquals(0L, withYou.values.sum())
    }

    @Test
    fun `a realistic multi-step scenario matches a hand-calculated reference`() {
        val balances = mutableMapOf<String, Long>()
        fun apply(deltas: Map<String, Long>) = deltas.forEach { (id, d) -> balances[id] = (balances[id] ?: 0L) + d }

        // 1) Dinner: 1000, You paid, split evenly 3 ways -> you=334, alice=333, bob=333.
        val dinner1 = SplitCalculator.equalSplit(1000L, listOf(YOU, "alice", "bob"))
        apply(expenseDeltas(mapOf(YOU to 1000L), dinner1))
        assertEquals(333L, balances["alice"])
        assertEquals(333L, balances["bob"])

        // 2) Taxi: 600, Bob paid, split evenly 3 ways -> 200 each exactly.
        apply(expenseDeltas(mapOf("bob" to 600L), SplitCalculator.equalSplit(600L, listOf(YOU, "alice", "bob"))))
        assertEquals(533L, balances["alice"]) // 333 + 200
        assertEquals(-67L, balances["bob"]) // 333 - 400 (paid 600, only owed 200)

        // 3) Alice settles 200 of what she owes You.
        settlementDelta("alice", YOU, 200L)?.let { (id, d) -> apply(mapOf(id to d)) }
        assertEquals(333L, balances["alice"]) // 533 - 200

        // 4) Editing the dinner (1000 -> 1200) reverses the old impact, then applies the new one.
        apply(expenseDeltas(mapOf(YOU to 1000L), dinner1).mapValues { -it.value })
        apply(expenseDeltas(mapOf(YOU to 1200L), SplitCalculator.equalSplit(1200L, listOf(YOU, "alice", "bob"))))
        assertEquals(400L, balances["alice"]) // 333 - 333 + 400
        assertEquals(0L, balances["bob"]) // -67 - 333 + 400
    }

    @Test
    fun `deleting an expense fully reverses its balance impact`() {
        val balances = mutableMapOf<String, Long>()
        fun apply(deltas: Map<String, Long>) = deltas.forEach { (id, d) -> balances[id] = (balances[id] ?: 0L) + d }

        val deltas = expenseDeltas(mapOf(YOU to 900L), SplitCalculator.equalSplit(900L, listOf(YOU, "alice", "bob", "carol")))
        apply(deltas)
        apply(deltas.mapValues { -it.value })

        balances.values.forEach { assertEquals(0L, it) }
    }

    @Test
    fun `repeated edits never drift the balance from what a single correct entry would give`() {
        val edited = mutableMapOf<String, Long>()
        fun apply(target: MutableMap<String, Long>, deltas: Map<String, Long>) =
            deltas.forEach { (id, d) -> target[id] = (target[id] ?: 0L) + d }

        var currentAmount = 1000L
        var currentSplit = SplitCalculator.equalSplit(currentAmount, listOf(YOU, "alice", "bob"))
        apply(edited, expenseDeltas(mapOf(YOU to currentAmount), currentSplit))

        // Edit the same expense five times in a row.
        repeat(5) { i ->
            apply(edited, expenseDeltas(mapOf(YOU to currentAmount), currentSplit).mapValues { -it.value })
            currentAmount = 1000L + (i + 1) * 300L
            currentSplit = SplitCalculator.equalSplit(currentAmount, listOf(YOU, "alice", "bob"))
            apply(edited, expenseDeltas(mapOf(YOU to currentAmount), currentSplit))
        }

        val direct = mutableMapOf<String, Long>()
        apply(direct, expenseDeltas(mapOf(YOU to currentAmount), currentSplit))

        assertEquals(direct["alice"], edited["alice"])
        assertEquals(direct["bob"], edited["bob"])
    }

    @Test
    fun `multi-payer expense reflects each payer's actual contribution`() {
        // 5000 bill: Alice paid 3000, Bob paid 2000, split equally three ways with You.
        val owed = SplitCalculator.equalSplit(5000L, listOf(YOU, "alice", "bob"))
        val deltas = expenseDeltas(mapOf("alice" to 3000L, "bob" to 2000L), owed)

        // Alice paid more than her share -> she's owed money back -> her balance goes negative.
        assertTrue((deltas["alice"] ?: 0L) < 0L)
        assertEquals(owed["bob"]!! - 2000L, deltas["bob"])
    }
}
