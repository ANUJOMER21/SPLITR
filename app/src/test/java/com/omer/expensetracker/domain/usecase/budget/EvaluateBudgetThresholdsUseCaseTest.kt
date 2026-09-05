package com.omer.expensetracker.domain.usecase.budget

import com.omer.expensetracker.domain.model.Budget
import com.omer.expensetracker.domain.model.BudgetMonthSnapshot
import com.omer.expensetracker.domain.model.BudgetProgress
import com.omer.expensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

private class FakeBudgetRepository : BudgetRepository {
    var budget = Budget("b1", categoryId = "cat1", monthlyLimitMinor = 10_000L, warningNotifiedMonth = null, breachNotifiedMonth = null, createdAt = 0L, updatedAt = 0L)
    var spentMinor = 0L

    override fun observeBudgetsWithProgress(month: YearMonth): Flow<List<BudgetProgress>> = flowOf(snapshot())
    override suspend fun getBudgetsWithProgressSnapshot(month: YearMonth): List<BudgetProgress> = snapshot()
    private fun snapshot() = listOf(BudgetProgress(budget, category = null, spentMinor = spentMinor, limitMinor = budget.monthlyLimitMinor))

    override suspend fun getBudgetHistory(month: YearMonth): List<BudgetMonthSnapshot> = emptyList()
    override suspend fun getBudget(id: String): Budget? = budget
    override suspend fun getBudgetForCategory(categoryId: String?): Budget? = budget
    override suspend fun upsertBudget(categoryId: String?, monthlyLimitMinor: Long): Budget = budget
    override suspend fun deleteBudget(id: String) {}
    override suspend fun markWarningNotified(id: String, yearMonth: String) {
        budget = budget.copy(warningNotifiedMonth = yearMonth)
    }
    override suspend fun markBreachNotified(id: String, yearMonth: String) {
        budget = budget.copy(breachNotifiedMonth = yearMonth)
    }
    override suspend fun getAllBudgets(): List<Budget> = listOf(budget)
}

class EvaluateBudgetThresholdsUseCaseTest {

    private lateinit var repository: FakeBudgetRepository
    private lateinit var useCase: EvaluateBudgetThresholdsUseCase

    @Before
    fun setUp() {
        repository = FakeBudgetRepository()
        useCase = EvaluateBudgetThresholdsUseCase(repository)
    }

    @Test
    fun `just under warning threshold fires nothing`() = runTest {
        repository.spentMinor = 7_999L // limit is 10_000, warning at 80% = 8_000
        val events = useCase(YearMonth.of(2026, 1))
        assertTrue(events.isEmpty())
    }

    @Test
    fun `exactly at warning threshold fires a warning event`() = runTest {
        repository.spentMinor = 8_000L
        val events = useCase(YearMonth.of(2026, 1))
        assertEquals(1, events.size)
        assertEquals(BudgetNotificationType.WARNING, events.first().type)
    }

    @Test
    fun `exactly at the limit fires a breach, not a warning`() = runTest {
        repository.spentMinor = 10_000L
        val events = useCase(YearMonth.of(2026, 1))
        assertEquals(1, events.size)
        assertEquals(BudgetNotificationType.BREACHED, events.first().type)
    }

    @Test
    fun `same month never fires the same threshold twice`() = runTest {
        repository.spentMinor = 8_500L
        val month = YearMonth.of(2026, 1)

        val first = useCase(month)
        val second = useCase(month)

        assertEquals(1, first.size)
        assertTrue(second.isEmpty())
    }

    @Test
    fun `a new month fires again even if still over the threshold`() = runTest {
        repository.spentMinor = 8_500L
        useCase(YearMonth.of(2026, 1))

        val events = useCase(YearMonth.of(2026, 2))

        assertEquals(1, events.size)
    }
}
