package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AddExpenseUseCaseTest {

    private lateinit var entryRepository: EntryRepository
    private lateinit var useCase: AddExpenseUseCase

    @Before
    fun setUp() {
        entryRepository = mockk()
        coEvery { entryRepository.addEntry(any()) } answers { firstArg() }
        useCase = AddExpenseUseCase(entryRepository)
    }

    @Test
    fun `rejects zero amount`() = runTest {
        val result = useCase(amountMinor = 0L, categoryId = "cat1", date = LocalDate.now())

        assertTrue(result is UseCaseResult.Failure)
        coVerify(exactly = 0) { entryRepository.addEntry(any()) }
    }

    @Test
    fun `rejects negative amount`() = runTest {
        val result = useCase(amountMinor = -500L, categoryId = "cat1", date = LocalDate.now())

        assertTrue(result is UseCaseResult.Failure)
    }

    @Test
    fun `rejects missing category`() = runTest {
        val result = useCase(amountMinor = 1000L, categoryId = null, date = LocalDate.now())

        assertTrue(result is UseCaseResult.Failure)
        assertEquals(
            "Category is required for an expense",
            (result as UseCaseResult.Failure).message
        )
    }

    @Test
    fun `rejects blank category`() = runTest {
        val result = useCase(amountMinor = 1000L, categoryId = "  ", date = LocalDate.now())

        assertTrue(result is UseCaseResult.Failure)
    }

    @Test
    fun `saves a valid expense`() = runTest {
        val date = LocalDate.of(2026, 1, 15)
        val result = useCase(amountMinor = 1500L, categoryId = "cat1", date = date)

        assertTrue(result is UseCaseResult.Success)
        val entry = (result as UseCaseResult.Success<Entry>).data
        assertEquals(1500L, entry.amountMinor)
        assertEquals("cat1", entry.categoryId)
        assertEquals(date, entry.date)
        coVerify(exactly = 1) { entryRepository.addEntry(any()) }
    }
}
