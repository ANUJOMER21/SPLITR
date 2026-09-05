package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.mapper.toEntity
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
import com.omer.expensetracker.domain.model.CategoryBreakdownItem
import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.MonthlySummary
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.service.WidgetRefresher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(
    private val entryDao: EntryDao,
    private val categoryDao: CategoryDao,
    private val syncOutbox: SyncOutbox,
    private val widgetRefresher: WidgetRefresher
) : EntryRepository {

    override fun observeFilteredEntries(filter: EntryFilter): Flow<List<Entry>> =
        entryDao.observeFiltered(EntryFilterQueryBuilder.build(filter)).map { list -> list.map { it.toDomain() } }

    override suspend fun getEntry(id: String): Entry? = entryDao.getById(id)?.toDomain()

    override suspend fun addEntry(entry: Entry): Entry {
        entryDao.insert(entry.toEntity())
        enqueueIfSyncable(entry, SyncOperation.UPSERT)
        widgetRefresher.refreshAll()
        return entry
    }

    override suspend fun updateEntry(entry: Entry) {
        entryDao.update(entry.toEntity())
        enqueueIfSyncable(entry, SyncOperation.UPSERT)
        widgetRefresher.refreshAll()
    }

    override suspend fun softDeleteEntry(id: String) {
        val existing = entryDao.getById(id)
        entryDao.softDelete(id, System.currentTimeMillis())
        if (existing != null) enqueueIfSyncable(existing.toDomain(), SyncOperation.DELETE)
        widgetRefresher.refreshAll()
    }

    override suspend fun restoreEntry(id: String) {
        entryDao.restore(id, System.currentTimeMillis())
        entryDao.getById(id)?.let { enqueueIfSyncable(it.toDomain(), SyncOperation.UPSERT) }
        widgetRefresher.refreshAll()
    }

    override suspend fun upsertFromRemote(entry: Entry) {
        val existing = entryDao.getById(entry.id)
        if (existing != null && existing.updatedAt >= entry.updatedAt) return
        // A category that hasn't synced yet must not break the FK — drop the reference; the
        // category listener fills it in later and a subsequent entry edit re-links it.
        val safeCategoryId = entry.categoryId?.takeIf { categoryDao.getById(it) != null }
        entryDao.upsert(entry.copy(categoryId = safeCategoryId).toEntity())
        widgetRefresher.refreshAll()
    }

    override suspend fun deleteFromRemote(id: String) {
        entryDao.softDelete(id, System.currentTimeMillis())
        widgetRefresher.refreshAll()
    }

    private suspend fun enqueueIfSyncable(entry: Entry, operation: String) {
        if (entry.linkedSharedExpenseId != null || entry.linkedGoalContributionId != null) return
        syncOutbox.enqueue(SyncEntityType.ENTRY, entry.id, operation)
    }

    override fun observeMonthlySummary(month: YearMonth): Flow<MonthlySummary> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return entryDao.observeMonthlyTotals(start, end).map { rows ->
            val expense = rows.firstOrNull { it.type == EntryType.EXPENSE.name }?.total ?: 0L
            val income = rows.firstOrNull { it.type == EntryType.INCOME.name }?.total ?: 0L
            MonthlySummary(totalExpenseMinor = expense, totalIncomeMinor = income)
        }
    }

    override fun observeCategoryBreakdown(month: YearMonth): Flow<List<CategoryBreakdownItem>> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return entryDao.observeCategoryBreakdown(start, end).map { rows ->
            val total = rows.sumOf { it.totalMinor }
            rows.map { row ->
                CategoryBreakdownItem(
                    category = row.category.toDomain(),
                    totalMinor = row.totalMinor,
                    percentage = if (total > 0) row.totalMinor * 100f / total else 0f
                )
            }
        }
    }
}
