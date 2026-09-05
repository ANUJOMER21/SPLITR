package com.omer.expensetracker.domain.usecase.budget

import com.omer.expensetracker.domain.model.Budget
import com.omer.expensetracker.domain.repository.BudgetRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpsertBudgetUseCaseTest {

    private lateinit var repository: BudgetRepository
    private lateinit var useCase: UpsertBudgetUseCase

    @Before
    fun setUp() {
        repository = mockk()
        coEvery { repository.upsertBudget(any(), any()) } answers {
            Budget("id", firstArg(), secondArg(), null, null, 0L, 0L)
        }
        useCase = UpsertBudgetUseCase(repository)
    }

    @Test
    fun `rejects a zero limit`() = runTest {
        val result = useCase("cat1", 0L)
        assertTrue(result is UseCaseResult.Failure)
        coVerify(exactly = 0) { repository.upsertBudget(any(), any()) }
    }

    @Test
    fun `rejects a negative limit`() = runTest {
        val result = useCase("cat1", -500L)
        assertTrue(result is UseCaseResult.Failure)
    }

    @Test
    fun `accepts a null category for the overall cap`() = runTest {
        val result = useCase(null, 50_000L)
        assertTrue(result is UseCaseResult.Success)
        coVerify(exactly = 1) { repository.upsertBudget(null, 50_000L) }
    }
}
