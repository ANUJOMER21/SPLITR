package com.omer.expensetracker.domain.usecase.split

import kotlin.math.abs
import kotlin.math.floor

/**
 * Pure split-calculation logic — no database or UI dependency, independently testable. Every
 * function here guarantees its returned shares sum *exactly* to the input total; nothing here
 * ever leaves a cent unaccounted for.
 */
object SplitCalculator {

    class SplitException(message: String) : Exception(message)

    /** Equal split — divides evenly among [participantIds]; any leftover cent goes to the
     * first participants in the order given, so the same input always produces the same
     * result. */
    fun equalSplit(totalMinor: Long, participantIds: List<String>): Map<String, Long> {
        require(participantIds.isNotEmpty()) { "At least one participant required" }
        val base = totalMinor / participantIds.size
        val remainder = totalMinor % participantIds.size
        return participantIds.mapIndexed { index, id -> id to base + if (index < remainder) 1 else 0 }.toMap()
    }

    /** Exact amounts — validates [amounts] sum exactly to [totalMinor] before accepting them. */
    fun exactSplit(totalMinor: Long, amounts: Map<String, Long>): Map<String, Long> {
        require(amounts.isNotEmpty()) { "At least one participant required" }
        val sum = amounts.values.sum()
        if (sum != totalMinor) throw SplitException("Amounts sum to $sum, expected $totalMinor")
        return amounts
    }

    /** Percentage split — [percentages] (0–100) must sum to 100 within [tolerance]; the
     * resulting minor-unit shares still sum exactly to [totalMinor]. */
    fun percentageSplit(totalMinor: Long, percentages: Map<String, Double>, tolerance: Double = 0.01): Map<String, Long> {
        require(percentages.isNotEmpty()) { "At least one participant required" }
        val sum = percentages.values.sum()
        if (abs(sum - 100.0) > tolerance) throw SplitException("Percentages sum to $sum, expected 100")
        return distributeProportionally(totalMinor, percentages)
    }

    /** Shares/ratio split (e.g. 2:1:1) — amounts computed proportionally to each share count. */
    fun sharesSplit(totalMinor: Long, shares: Map<String, Int>): Map<String, Long> {
        require(shares.isNotEmpty()) { "At least one participant required" }
        require(shares.values.all { it > 0 }) { "Shares must be positive" }
        return distributeProportionally(totalMinor, shares.mapValues { it.value.toDouble() })
    }

    data class ItemInput(val id: String, val amountMinor: Long, val participantIds: List<String>)

    /** Itemized split — each item's cost is divided evenly among the people assigned to it
     * (via [equalSplit]'s remainder rule), then [taxAndTipMinor] is distributed proportionally
     * to each participant's resulting item subtotal. */
    fun itemizedSplit(items: List<ItemInput>, taxAndTipMinor: Long = 0L): Map<String, Long> {
        require(items.isNotEmpty()) { "At least one item required" }
        val perPersonItemTotal = LinkedHashMap<String, Long>()
        items.forEach { item ->
            require(item.participantIds.isNotEmpty()) { "Item ${item.id} has no participants assigned" }
            equalSplit(item.amountMinor, item.participantIds).forEach { (personId, amount) ->
                perPersonItemTotal[personId] = (perPersonItemTotal[personId] ?: 0L) + amount
            }
        }
        val itemsTotal = perPersonItemTotal.values.sum()
        if (taxAndTipMinor == 0L || itemsTotal == 0L) return perPersonItemTotal
        val taxShare = distributeProportionally(taxAndTipMinor, perPersonItemTotal.mapValues { it.value.toDouble() })
        return perPersonItemTotal.mapValues { (id, amount) -> amount + (taxShare[id] ?: 0L) }
    }

    /** Splits [totalMinor] proportionally to [weights] using the largest-remainder method: each
     * share is floored, then leftover cents go one-by-one — largest fractional remainder first,
     * ties broken by [weights]' iteration order — so the result always sums exactly to
     * [totalMinor] and is deterministic for a given input. */
    private fun distributeProportionally(totalMinor: Long, weights: Map<String, Double>): Map<String, Long> {
        val totalWeight = weights.values.sum()
        val raw = weights.mapValues { (_, w) -> totalMinor * (w / totalWeight) }
        val floored = raw.mapValues { (_, v) -> floor(v).toLong() }
        var remainder = totalMinor - floored.values.sum()
        if (remainder <= 0) return floored
        val byRemainderDesc = raw.entries.sortedWith(
            compareByDescending<Map.Entry<String, Double>> { it.value - floor(it.value) }
        ).map { it.key }
        val result = floored.toMutableMap()
        for (id in byRemainderDesc) {
            if (remainder <= 0) break
            result[id] = (result[id] ?: 0L) + 1
            remainder--
        }
        return result
    }
}
