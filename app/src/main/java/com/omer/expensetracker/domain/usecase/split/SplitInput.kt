package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.model.split.SplitType

/** The raw, not-yet-validated split choice a user makes on the Add/Edit Shared Expense screen —
 * [SplitCalculator] turns this into per-person owed shares that always sum to the total. */
sealed class SplitInput {
    data class Equal(val participantIds: List<String>) : SplitInput()
    data class Exact(val amounts: Map<String, Long>) : SplitInput()
    data class Percentage(val percentages: Map<String, Double>) : SplitInput()
    data class Shares(val shares: Map<String, Int>) : SplitInput()
    data class Itemized(val items: List<SplitCalculator.ItemInput>, val taxAndTipMinor: Long = 0L) : SplitInput()
}

internal fun computeOwedShares(amountMinor: Long, split: SplitInput): Pair<Map<String, Long>, SplitType> = when (split) {
    is SplitInput.Equal -> SplitCalculator.equalSplit(amountMinor, split.participantIds) to SplitType.EQUAL
    is SplitInput.Exact -> SplitCalculator.exactSplit(amountMinor, split.amounts) to SplitType.EXACT
    is SplitInput.Percentage -> SplitCalculator.percentageSplit(amountMinor, split.percentages) to SplitType.PERCENTAGE
    is SplitInput.Shares -> SplitCalculator.sharesSplit(amountMinor, split.shares) to SplitType.SHARES
    is SplitInput.Itemized -> SplitCalculator.itemizedSplit(split.items, split.taxAndTipMinor) to SplitType.ITEMIZED
}
