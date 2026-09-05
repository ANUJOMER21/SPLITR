package com.omer.expensetracker.domain.model

import java.time.LocalDate

/**
 * amountMinor is the whole-number minor unit (e.g. paise/cents) — never a Double —
 * so totals never accumulate floating-point drift.
 */
data class Entry(
    val id: String,
    val type: EntryType,
    val amountMinor: Long,
    val categoryId: String?,
    val date: LocalDate,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val linkedSharedExpenseId: String? = null
)
