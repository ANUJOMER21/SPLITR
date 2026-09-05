package com.omer.expensetracker.domain.repository

import com.omer.expensetracker.domain.model.CategoryBreakdownItem
import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.model.MonthlySummary
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface EntryRepository {
    fun observeFilteredEntries(filter: EntryFilter): Flow<List<Entry>>
    suspend fun getEntry(id: String): Entry?
    suspend fun addEntry(entry: Entry): Entry
    suspend fun updateEntry(entry: Entry)
    suspend fun softDeleteEntry(id: String)
    suspend fun restoreEntry(id: String)

    /** Cloud-sync-only: merge an entry pulled from this account's Firestore backup. Linked
     * entries (mirrors of a shared expense / goal contribution) are never synced this way — they
     * are re-derived on each device from their source. */
    suspend fun upsertFromRemote(entry: Entry)
    suspend fun deleteFromRemote(id: String)
    fun observeMonthlySummary(month: YearMonth): Flow<MonthlySummary>
    fun observeCategoryBreakdown(month: YearMonth): Flow<List<CategoryBreakdownItem>>
}
