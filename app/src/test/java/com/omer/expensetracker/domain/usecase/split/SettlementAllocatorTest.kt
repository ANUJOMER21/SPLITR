package com.omer.expensetracker.domain.usecase.split

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettlementAllocatorTest {

    @Test
    fun `exact amount clears every bucket`() {
        val out = SettlementAllocator.allocate(mapOf("g1" to 300L, "g2" to 200L, null to 100L), 600L)
        assertEquals(mapOf("g1" to 300L, "g2" to 200L, null to 100L), out)
        assertEquals(600L, out.values.sum())
    }

    @Test
    fun `underpayment fills the largest bucket first`() {
        val out = SettlementAllocator.allocate(mapOf("g1" to 300L, "g2" to 200L, null to 100L), 350L)
        assertEquals(300L, out["g1"])
        assertEquals(50L, out["g2"])
        assertEquals(null, out[null])
        assertEquals(350L, out.values.sum())
    }

    @Test
    fun `overpayment adds the remainder to the largest bucket`() {
        val out = SettlementAllocator.allocate(mapOf("g1" to 300L, "g2" to 200L), 700L)
        assertEquals(500L, out["g1"]) // 300 outstanding + 200 remainder
        assertEquals(200L, out["g2"])
        assertEquals(700L, out.values.sum())
    }

    @Test
    fun `single bucket takes the whole amount`() {
        assertEquals(mapOf("g1" to 420L), SettlementAllocator.allocate(mapOf("g1" to 999L), 420L))
    }

    @Test
    fun `non-positive buckets are ignored`() {
        val out = SettlementAllocator.allocate(mapOf("g1" to 500L, "g2" to 0L, "g3" to -100L), 500L)
        assertEquals(mapOf("g1" to 500L), out)
    }

    @Test
    fun `zero or negative total yields nothing`() {
        assertTrue(SettlementAllocator.allocate(mapOf("g1" to 300L), 0L).isEmpty())
        assertTrue(SettlementAllocator.allocate(mapOf("g1" to 300L), -50L).isEmpty())
    }

    @Test
    fun `no positive buckets yields nothing`() {
        assertTrue(SettlementAllocator.allocate(mapOf("g1" to 0L, null to 0L), 100L).isEmpty())
    }

    @Test
    fun `result always sums to the requested total`() {
        val buckets = mapOf("g1" to 137L, "g2" to 891L, "g3" to 44L, null to 512L)
        listOf(1L, 50L, 400L, 1584L, 5000L).forEach { total ->
            assertEquals("total=$total", total, SettlementAllocator.allocate(buckets, total).values.sum())
        }
    }
}
