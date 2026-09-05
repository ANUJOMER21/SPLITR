package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.mapper.toEntity
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
    private val widgetRefresher: WidgetRefresher
) : EntryRepository {

    override fun observeFilteredEntries(filter: EntryFilter): Flow<List<Entry>> =
        entryDao.observeFiltered(EntryFilterQueryBuilder.build(filter)).map { list -> list.map { it.toDomain() } }

    override suspend fun getEntry(id: String): Entry? = entryDao.getById(id)?.toDomain()

    override suspend fun addEntry(entry: Entry): Entry {
        entryDao.insert(entry.toEntity())
        widgetRefresher.refreshAll()
        return entry
    }

    override suspend fun updateEntry(entry: Entry) {
        entryDao.update(entry.toEntity())
        widgetRefresher.refreshAll()
    }

    override suspend fun softDeleteEntry(id: String) {
        entryDao.softDelete(id, System.currentTimeMillis())
        widgetRefresher.refreshAll()
    }

    override suspend fun restoreEntry(id: String) {
        entryDao.restore(id, System.currentTimeMillis())
        widgetRefresher.refreshAll()
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
