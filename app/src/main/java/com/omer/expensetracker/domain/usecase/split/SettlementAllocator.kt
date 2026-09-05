package com.omer.expensetracker.domain.usecase.split

/**
 * Pure, independently testable split of one friend-level settlement across the buckets that
 * produced the debt — each group the two people share, plus the non-group ("direct") bucket
 * keyed by `null`. No database or UI dependency.
 *
 * The caller has already picked the settlement direction and reduced each bucket to a positive
 * outstanding magnitude (how much this bucket could absorb). [allocate] fills the largest
 * bucket first, caps each at its outstanding magnitude, and — if the payment exceeds every
 * outstanding balance combined — drops the leftover on the largest bucket, so the returned
 * amounts always sum to exactly the amount paid.
 */
object SettlementAllocator {

    /**
     * @param outstandingByBucket bucket key (`null` = non-group) → positive outstanding magnitude.
     * @param totalAmountMinor positive amount actually being paid.
     * @return bucket key → amount that slice settles; every value > 0, summing to
     *   [totalAmountMinor]. Empty when there is nothing to allocate.
     */
    fun allocate(outstandingByBucket: Map<String?, Long>, totalAmountMinor: Long): Map<String?, Long> {
        if (totalAmountMinor <= 0L) return emptyMap()

        val ordered = outstandingByBucket
            .filterValues { it > 0L }
            .toList()
            .sortedByDescending { it.second }
        if (ordered.isEmpty()) return emptyMap()

        val result = LinkedHashMap<String?, Long>()
        var remaining = totalAmountMinor
        for ((bucket, outstanding) in ordered) {
            if (remaining <= 0L) break
            val take = minOf(remaining, outstanding)
            result[bucket] = take
            remaining -= take
        }

        // Overpayment beyond every outstanding balance lands on the biggest bucket.
        if (remaining > 0L) {
            val biggest = ordered.first().first
            result[biggest] = (result[biggest] ?: 0L) + remaining
        }

        return result
    }
}
