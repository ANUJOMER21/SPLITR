package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AddIncomeUseCaseTest {

    private lateinit var entryRepository: EntryRepository
    private lateinit var useCase: AddIncomeUseCase

    @Before
    fun setUp() {
        entryRepository = mockk()
        coEvery { entryRepository.addEntry(any()) } answers { firstArg() }
        useCase = AddIncomeUseCase(entryRepository)
    }

    @Test
    fun `rejects zero amount`() = runTest {
        val result = useCase(amountMinor = 0L, date = LocalDate.now())
        assertTrue(result is UseCaseResult.Failure)
    }

    @Test
    fun `does not require a category`() = runTest {
        val result = useCase(amountMinor = 2500L, date = LocalDate.now())

        assertTrue(result is UseCaseResult.Success)
        val entry = (result as UseCaseResult.Success<Entry>).data
        assertNull(entry.categoryId)
        assertEquals(2500L, entry.amountMinor)
    }
}
