package com.omer.expensetracker.domain.usecase.split

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SplitCalculatorTest {

    // ---- equal split ----

    @Test
    fun `equal split divides evenly when it divides exactly`() {
        val result = SplitCalculator.equalSplit(1000L, listOf("a", "b", "c", "d"))
        assertEquals(mapOf("a" to 250L, "b" to 250L, "c" to 250L, "d" to 250L), result)
    }

    @Test
    fun `equal split gives leftover cents to the first participants in order`() {
        // 1000 / 3 = 333 remainder 1 -> first participant gets the extra cent
        val result = SplitCalculator.equalSplit(1000L, listOf("a", "b", "c"))
        assertEquals(334L, result["a"])
        assertEquals(333L, result["b"])
        assertEquals(333L, result["c"])
        assertEquals(1000L, result.values.sum())
    }

    @Test
    fun `equal split of a single cent among many still sums exactly`() {
        val result = SplitCalculator.equalSplit(1L, listOf("a", "b", "c"))
        assertEquals(1L, result.values.sum())
        assertEquals(1L, result["a"])
        assertEquals(0L, result["b"])
        assertEquals(0L, result["c"])
    }

    @Test
    fun `equal split is deterministic for the same input`() {
        val ids = listOf("a", "b", "c", "d", "e", "f", "g")
        val first = SplitCalculator.equalSplit(999L, ids)
        val second = SplitCalculator.equalSplit(999L, ids)
        assertEquals(first, second)
    }

    // ---- exact split ----

    @Test
    fun `exact split accepts amounts that sum to the total`() {
        val result = SplitCalculator.exactSplit(1000L, mapOf("a" to 600L, "b" to 400L))
        assertEquals(1000L, result.values.sum())
    }

    @Test
    fun `exact split rejects amounts that do not sum to the total`() {
        assertThrows(SplitCalculator.SplitException::class.java) {
            SplitCalculator.exactSplit(1000L, mapOf("a" to 600L, "b" to 300L))
        }
    }

    // ---- percentage split ----

    @Test
    fun `percentage split sums exactly to the total despite rounding`() {
        // 33.33 / 33.33 / 33.34 of 100 -> classic rounding trap, must still sum to 100
        val result = SplitCalculator.percentageSplit(100L, mapOf("a" to 33.33, "b" to 33.33, "c" to 33.34))
        assertEquals(100L, result.values.sum())
    }

    @Test
    fun `percentage split rejects percentages that do not sum to 100`() {
        assertThrows(SplitCalculator.SplitException::class.java) {
            SplitCalculator.percentageSplit(1000L, mapOf("a" to 50.0, "b" to 40.0))
        }
    }

    @Test
    fun `percentage split tolerates tiny floating point drift`() {
        val result = SplitCalculator.percentageSplit(1000L, mapOf("a" to 33.34, "b" to 33.33, "c" to 33.329999999))
        assertEquals(1000L, result.values.sum())
    }

    // ---- shares split ----

    @Test
    fun `shares split divides proportionally and sums exactly`() {
        val result = SplitCalculator.sharesSplit(100L, mapOf("a" to 2, "b" to 1, "c" to 1))
        assertEquals(100L, result.values.sum())
        assertEquals(50L, result["a"])
    }

    @Test
    fun `shares split with an odd total still sums exactly`() {
        val result = SplitCalculator.sharesSplit(101L, mapOf("a" to 2, "b" to 1, "c" to 1))
        assertEquals(101L, result.values.sum())
    }

    // ---- itemized split ----

    @Test
    fun `itemized split assigns each item only to its participants`() {
        val items = listOf(
            SplitCalculator.ItemInput("pizza", 1000L, listOf("a", "b")),
            SplitCalculator.ItemInput("soda", 200L, listOf("b"))
        )
        val result = SplitCalculator.itemizedSplit(items)
        assertEquals(500L, result["a"])
        assertEquals(700L, result["b"])
        assertEquals(1200L, result.values.sum())
    }

    @Test
    fun `itemized split distributes tax and tip proportionally to item subtotals`() {
        val items = listOf(
            SplitCalculator.ItemInput("steak", 3000L, listOf("a")),
            SplitCalculator.ItemInput("salad", 1000L, listOf("b"))
        )
        // a spent 3x what b spent on items -> a should absorb 3x the tax/tip too, and the
        // total (items + tax/tip) must still sum exactly.
        val result = SplitCalculator.itemizedSplit(items, taxAndTipMinor = 400L)
        assertEquals(4400L, result.values.sum())
        assertEquals(3300L, result["a"])
        assertEquals(1100L, result["b"])
    }

    @Test
    fun `itemized split with no tax still sums exactly`() {
        val items = listOf(SplitCalculator.ItemInput("coffee", 333L, listOf("a", "b", "c")))
        val result = SplitCalculator.itemizedSplit(items)
        assertEquals(333L, result.values.sum())
    }
}
