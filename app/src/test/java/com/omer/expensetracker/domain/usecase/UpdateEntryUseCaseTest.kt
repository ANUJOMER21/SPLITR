package com.omer.expensetracker.domain.usecase

import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class UpdateEntryUseCaseTest {

    private lateinit var entryRepository: EntryRepository
    private lateinit var useCase: UpdateEntryUseCase

    @Before
    fun setUp() {
        entryRepository = mockk()
        coEvery { entryRepository.updateEntry(any()) } returns Unit
        useCase = UpdateEntryUseCase(entryRepository)
    }

    private fun expense(amountMinor: Long = 1000L, categoryId: String? = "cat1") = Entry(
        id = "e1",
        type = EntryType.EXPENSE,
        amountMinor = amountMinor,
        categoryId = categoryId,
        date = LocalDate.now(),
        createdAt = 0L,
        updatedAt = 0L
    )

    @Test
    fun `rejects zero amount`() = runTest {
        val result = useCase(expense(amountMinor = 0L))
        assertTrue(result is UseCaseResult.Failure)
        coVerify(exactly = 0) { entryRepository.updateEntry(any()) }
    }

    @Test
    fun `rejects expense with no category`() = runTest {
        val result = useCase(expense(categoryId = null))
        assertTrue(result is UseCaseResult.Failure)
    }

    @Test
    fun `saves a valid update`() = runTest {
        val result = useCase(expense())
        assertTrue(result is UseCaseResult.Success)
        coVerify(exactly = 1) { entryRepository.updateEntry(any()) }
    }
}
