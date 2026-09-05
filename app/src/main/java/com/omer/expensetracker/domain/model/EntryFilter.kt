package com.omer.expensetracker.domain.model

import java.time.LocalDate

data class EntryFilter(
    val type: EntryType? = null,
    val categoryIds: Set<String> = emptySet(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minAmountMinor: Long? = null,
    val maxAmountMinor: Long? = null,
    val searchQuery: String = ""
) {
    val isActive: Boolean
        get() = type != null || categoryIds.isNotEmpty() || startDate != null || endDate != null ||
            minAmountMinor != null || maxAmountMinor != null || searchQuery.isNotBlank()
}

enum class DateRangePreset {
    THIS_WEEK, THIS_MONTH, LAST_MONTH, THIS_YEAR, ALL_TIME, CUSTOM
}
