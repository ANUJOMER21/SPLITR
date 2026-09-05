package com.omer.expensetracker.domain.model

import java.time.LocalDate

data class RecurringRule(
    val id: String,
    val type: EntryType,
    val amountMinor: Long,
    val categoryId: String?,
    val unit: RecurrenceUnit,
    val intervalCount: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val lastGeneratedDate: LocalDate?,
    val nextDueDate: LocalDate,
    val isPaused: Boolean,
    val skipNextOccurrence: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
