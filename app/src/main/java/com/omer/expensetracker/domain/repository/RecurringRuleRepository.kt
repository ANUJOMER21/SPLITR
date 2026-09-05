package com.omer.expensetracker.domain.repository

import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.model.RecurringRule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RecurringRuleRepository {
    fun observeAll(): Flow<List<RecurringRule>>
    suspend fun getById(id: String): RecurringRule?

    suspend fun addRule(
        type: EntryType,
        amountMinor: Long,
        categoryId: String?,
        unit: RecurrenceUnit,
        intervalCount: Int,
        startDate: LocalDate,
        endDate: LocalDate?
    ): RecurringRule

    suspend fun updateRule(rule: RecurringRule)
    suspend fun deleteRule(id: String)
    suspend fun setPaused(id: String, isPaused: Boolean)
    suspend fun setSkipNextOccurrence(id: String, skip: Boolean)

    /** Rules due on or before [today] that are neither paused nor past their end date. */
    suspend fun getDueRules(today: LocalDate): List<RecurringRule>
    suspend fun recordGenerated(id: String, generatedDate: LocalDate, nextDueDate: LocalDate)
}
