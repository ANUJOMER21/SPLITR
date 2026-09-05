package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

private class FakeRecurringRuleRepository : RecurringRuleRepository {
    val rules = mutableMapOf<String, RecurringRule>()

    override fun observeAll(): Flow<List<RecurringRule>> = flowOf(rules.values.toList())
    override suspend fun getById(id: String): RecurringRule? = rules[id]

    override suspend fun addRule(
        type: EntryType, amountMinor: Long, categoryId: String?, unit: RecurrenceUnit,
        intervalCount: Int, startDate: LocalDate, endDate: LocalDate?
    ): RecurringRule {
        val rule = RecurringRule(
            UUID.randomUUID().toString(), type, amountMinor, categoryId, unit, intervalCount,
            startDate, endDate, null, startDate, false, false, 0L, 0L
        )
        rules[rule.id] = rule
        return rule
    }

    override suspend fun updateRule(rule: RecurringRule) { rules[rule.id] = rule }
    override suspend fun deleteRule(id: String) { rules.remove(id) }
    override suspend fun upsertFromRemote(rule: com.omer.expensetracker.domain.model.RecurringRule) {}
    override suspend fun deleteFromRemote(id: String) {}
    override suspend fun setPaused(id: String, isPaused: Boolean) {
        rules[id]?.let { rules[id] = it.copy(isPaused = isPaused) }
    }
    override suspend fun setSkipNextOccurrence(id: String, skip: Boolean) {
        rules[id]?.let { rules[id] = it.copy(skipNextOccurrence = skip) }
    }

    override suspend fun getDueRules(today: LocalDate): List<RecurringRule> =
        rules.values.filter { !it.isPaused && !it.nextDueDate.isAfter(today) && (it.endDate == null || !it.nextDueDate.isAfter(it.endDate)) }

    override suspend fun recordGenerated(id: String, generatedDate: LocalDate, nextDueDate: LocalDate) {
        rules[id]?.let { rules[id] = it.copy(lastGeneratedDate = generatedDate, nextDueDate = nextDueDate) }
    }
}

private class FakeEntryRepository : EntryRepository {
    val entries = mutableListOf<Entry>()

    override fun observeFilteredEntries(filter: EntryFilter): Flow<List<Entry>> = flowOf(entries)
    override suspend fun getEntry(id: String): Entry? = entries.find { it.id == id }
    override suspend fun addEntry(entry: Entry): Entry {
        entries += entry
        return entry
    }
    override suspend fun updateEntry(entry: Entry) {}
    override suspend fun softDeleteEntry(id: String) {}
    override suspend fun restoreEntry(id: String) {}
    override suspend fun upsertFromRemote(entry: com.omer.expensetracker.domain.model.Entry) {}
    override suspend fun deleteFromRemote(id: String) {}
    override fun observeMonthlySummary(month: YearMonth) = flowOf(com.omer.expensetracker.domain.model.MonthlySummary(0, 0))
    override fun observeCategoryBreakdown(month: YearMonth) = flowOf(emptyList<com.omer.expensetracker.domain.model.CategoryBreakdownItem>())
}

class GenerateDueRecurringEntriesUseCaseTest {

    private lateinit var ruleRepository: FakeRecurringRuleRepository
    private lateinit var entryRepository: FakeEntryRepository
    private lateinit var useCase: GenerateDueRecurringEntriesUseCase

    @Before
    fun setUp() {
        ruleRepository = FakeRecurringRuleRepository()
        entryRepository = FakeEntryRepository()
        useCase = GenerateDueRecurringEntriesUseCase(ruleRepository, entryRepository)
    }

    @Test
    fun `generates every missed occurrence after a long gap, exactly once`() = runTest {
        val rule = ruleRepository.addRule(
            EntryType.INCOME, 1000L, null, RecurrenceUnit.MONTH, 1,
            startDate = LocalDate.of(2026, 1, 1), endDate = null
        )
        val today = LocalDate.of(2026, 4, 1)

        val generated = useCase(today)

        assertEquals(4, generated.size) // Jan 1, Feb 1, Mar 1, Apr 1
        assertEquals(4, entryRepository.entries.size)
        val updatedRule = ruleRepository.getById(rule.id)!!
        assertEquals(LocalDate.of(2026, 5, 1), updatedRule.nextDueDate)
    }

    @Test
    fun `running twice on the same day never duplicates entries`() = runTest {
        ruleRepository.addRule(EntryType.INCOME, 500L, null, RecurrenceUnit.DAY, 1, LocalDate.of(2026, 1, 1), null)
        val today = LocalDate.of(2026, 1, 5)

        useCase(today)
        val countAfterFirstRun = entryRepository.entries.size
        useCase(today)

        assertEquals(countAfterFirstRun, entryRepository.entries.size)
    }

    @Test
    fun `skipNextOccurrence omits exactly one occurrence then clears itself`() = runTest {
        val rule = ruleRepository.addRule(EntryType.INCOME, 500L, null, RecurrenceUnit.MONTH, 1, LocalDate.of(2026, 1, 1), null)
        ruleRepository.setSkipNextOccurrence(rule.id, true)

        val generated = useCase(LocalDate.of(2026, 3, 1))

        // Jan 1 skipped, Feb 1 and Mar 1 generated.
        assertEquals(2, generated.size)
        assertTrue(ruleRepository.getById(rule.id)!!.skipNextOccurrence.not())
    }

    @Test
    fun `paused rules are never picked up`() = runTest {
        val rule = ruleRepository.addRule(EntryType.INCOME, 500L, null, RecurrenceUnit.DAY, 1, LocalDate.of(2026, 1, 1), null)
        ruleRepository.setPaused(rule.id, true)

        val generated = useCase(LocalDate.of(2026, 1, 10))

        assertTrue(generated.isEmpty())
    }
}
