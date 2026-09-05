package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.RecurringRuleDao
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.mapper.toEntity
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class RecurringRuleRepositoryImpl @Inject constructor(
    private val recurringRuleDao: RecurringRuleDao,
    private val categoryDao: CategoryDao,
    private val syncOutbox: SyncOutbox
) : RecurringRuleRepository {

    override fun observeAll(): Flow<List<RecurringRule>> =
        recurringRuleDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): RecurringRule? = recurringRuleDao.getById(id)?.toDomain()

    override suspend fun addRule(
        type: EntryType,
        amountMinor: Long,
        categoryId: String?,
        unit: RecurrenceUnit,
        intervalCount: Int,
        startDate: LocalDate,
        endDate: LocalDate?
    ): RecurringRule {
        val now = System.currentTimeMillis()
        val rule = RecurringRule(
            id = UUID.randomUUID().toString(),
            type = type,
            amountMinor = amountMinor,
            categoryId = categoryId,
            unit = unit,
            intervalCount = intervalCount,
            startDate = startDate,
            endDate = endDate,
            lastGeneratedDate = null,
            nextDueDate = startDate,
            isPaused = false,
            skipNextOccurrence = false,
            createdAt = now,
            updatedAt = now
        )
        recurringRuleDao.insert(rule.toEntity())
        syncOutbox.enqueue(SyncEntityType.RECURRING_RULE, rule.id, SyncOperation.UPSERT)
        return rule
    }

    override suspend fun updateRule(rule: RecurringRule) {
        recurringRuleDao.update(rule.toEntity())
        syncOutbox.enqueue(SyncEntityType.RECURRING_RULE, rule.id, SyncOperation.UPSERT)
    }

    override suspend fun deleteRule(id: String) {
        recurringRuleDao.softDelete(id, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.RECURRING_RULE, id, SyncOperation.DELETE)
    }

    override suspend fun setPaused(id: String, isPaused: Boolean) {
        recurringRuleDao.setPaused(id, isPaused, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.RECURRING_RULE, id, SyncOperation.UPSERT)
    }

    override suspend fun setSkipNextOccurrence(id: String, skip: Boolean) {
        recurringRuleDao.setSkipNextOccurrence(id, skip, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.RECURRING_RULE, id, SyncOperation.UPSERT)
    }

    override suspend fun upsertFromRemote(rule: RecurringRule) {
        val existing = recurringRuleDao.getById(rule.id)
        if (existing != null && existing.updatedAt >= rule.updatedAt) return
        val safeCategoryId = rule.categoryId?.takeIf { categoryDao.getById(it) != null }
        recurringRuleDao.upsert(rule.copy(categoryId = safeCategoryId).toEntity())
    }

    override suspend fun deleteFromRemote(id: String) {
        recurringRuleDao.softDelete(id, System.currentTimeMillis())
    }

    override suspend fun getDueRules(today: LocalDate): List<RecurringRule> =
        recurringRuleDao.getDueRules(today.toEpochDay()).map { it.toDomain() }

    override suspend fun recordGenerated(id: String, generatedDate: LocalDate, nextDueDate: LocalDate) {
        recurringRuleDao.recordGenerated(
            id, generatedDate.toEpochDay(), nextDueDate.toEpochDay(), System.currentTimeMillis()
        )
        // Sync the advanced due-date so another device doesn't re-generate the same occurrence.
        syncOutbox.enqueue(SyncEntityType.RECURRING_RULE, id, SyncOperation.UPSERT)
    }
}
