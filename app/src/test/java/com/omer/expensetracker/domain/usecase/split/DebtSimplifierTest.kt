package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtSimplifierTest {

    private fun settle(balances: Map<String, Long>, payments: List<DebtSimplifier.Payment>): Map<String, Long> {
        val result = balances.toMutableMap()
        payments.forEach { p ->
            result[p.fromFriendId] = (result[p.fromFriendId] ?: 0L) + p.amountMinor
            result[p.toFriendId] = (result[p.toFriendId] ?: 0L) - p.amountMinor
        }
        return result
    }

    @Test
    fun `simple two person debt produces one payment`() {
        val payments = DebtSimplifier.simplify(mapOf("a" to 500L, "b" to -500L))
        assertEquals(1, payments.size)
        assertEquals(DebtSimplifier.Payment("b", "a", 500L), payments[0])
    }

    @Test
    fun `all-zero balances produce no payments`() {
        assertTrue(DebtSimplifier.simplify(mapOf("a" to 0L, "b" to 0L)).isEmpty())
    }

    @Test
    fun `three person chain simplifies to fewer transactions than the raw pairwise debts`() {
        // a paid for everyone: b owes 300, c owes 300 -> a is owed 600 overall.
        val balances = mapOf("a" to 600L, "b" to -300L, "c" to -300L)
        val payments = DebtSimplifier.simplify(balances)
        assertTrue(payments.size <= 2)
        assertEqualsSettledToZero(balances, payments)
    }

    @Test
    fun `complex group settles everyone to exactly zero with no more transactions than pairwise debts`() {
        // A owes B 100, B owes C 100, C owes D 100 (a chain) -> nets: A=-100, B=0, C=0, D=+100
        val balances = mapOf("a" to -100L, "b" to 0L, "c" to 0L, "d" to 100L)
        val payments = DebtSimplifier.simplify(balances)
        assertTrue(payments.size <= 3)
        assertEqualsSettledToZero(balances, payments)
    }

    @Test
    fun `multiple creditors and debtors settle to exactly zero`() {
        val balances = mapOf("a" to 700L, "b" to 300L, "c" to -400L, "d" to -600L)
        val payments = DebtSimplifier.simplify(balances)
        assertTrue(payments.size <= 3)
        assertEqualsSettledToZero(balances, payments)
    }

    private fun assertEqualsSettledToZero(balances: Map<String, Long>, payments: List<DebtSimplifier.Payment>) {
        val settled = settle(balances, payments)
        settled.values.forEach { assertEquals(0L, it) }
    }

    // ---- toCreditorDebtorMap: regression coverage for a sign-convention bug caught by manual
    // testing — Balance.netMinor (positive = the friend owes the owner) is the *opposite* sign
    // of what DebtSimplifier expects (positive = is owed money). ----

    @Test
    fun `toCreditorDebtorMap flips a friend who owes the owner into a debtor, not a creditor`() {
        // Alice owes the owner 500 -> in creditor terms Alice is the debtor (-500) and the
        // owner is the creditor (+500), so simplify must produce "alice pays you", not the
        // reverse.
        val net = DebtSimplifier.toCreditorDebtorMap(mapOf("alice" to 500L))
        val payments = DebtSimplifier.simplify(net)
        assertEquals(1, payments.size)
        assertEquals("alice", payments[0].fromFriendId)
        assertEquals(YOU_FRIEND_ID, payments[0].toFriendId)
        assertEquals(500L, payments[0].amountMinor)
    }

    @Test
    fun `toCreditorDebtorMap flips a friend the owner owes into a creditor`() {
        // The owner owes Bob 300 -> Bob's raw balance is -300 (owner owes them), so Bob should
        // come out as the creditor and the owner as the debtor.
        val net = DebtSimplifier.toCreditorDebtorMap(mapOf("bob" to -300L))
        val payments = DebtSimplifier.simplify(net)
        assertEquals(1, payments.size)
        assertEquals(YOU_FRIEND_ID, payments[0].fromFriendId)
        assertEquals("bob", payments[0].toFriendId)
        assertEquals(300L, payments[0].amountMinor)
    }

    @Test
    fun `toCreditorDebtorMap always nets to exactly zero`() {
        val net = DebtSimplifier.toCreditorDebtorMap(mapOf("alice" to 500L, "bob" to -300L, "carol" to 120L))
        assertEquals(0L, net.values.sum())
    }
}
