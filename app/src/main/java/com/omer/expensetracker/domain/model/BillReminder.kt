package com.omer.expensetracker.domain.model

import java.time.LocalDate

data class BillReminder(
    val id: String,
    val label: String,
    val estimatedAmountMinor: Long?,
    val categoryId: String?,
    val dueDate: LocalDate,
    val leadDays: Int,
    val repeatUnit: RecurrenceUnit?,
    val repeatIntervalCount: Int?,
    val isPaid: Boolean,
    val paidEntryId: String?,
    val lastNotifiedForDueDate: LocalDate?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isRepeating: Boolean get() = repeatUnit != null
    fun isOverdueUnpaid(today: LocalDate): Boolean = !isPaid && dueDate.isBefore(today)
}
