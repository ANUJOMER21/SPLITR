package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddCategoryUseCaseTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var useCase: AddCategoryUseCase

    @Before
    fun setUp() {
        categoryRepository = mockk()
        coEvery { categoryRepository.addCategory(any(), any(), any()) } answers {
            Category(
                id = "new",
                name = firstArg(),
                iconKey = secondArg(),
                colorArgb = thirdArg(),
                isDefault = false,
                isActive = true,
                createdAt = 0L,
                updatedAt = 0L
            )
        }
        useCase = AddCategoryUseCase(categoryRepository)
    }

    @Test
    fun `rejects empty name`() = runTest {
        val result = useCase("   ", "other", 0xFF000000)
        assertTrue(result is UseCaseResult.Failure)
        coVerify(exactly = 0) { categoryRepository.addCategory(any(), any(), any()) }
    }

    @Test
    fun `trims and saves a valid name`() = runTest {
        val result = useCase("  Subscriptions  ", "other", 0xFF000000)
        assertTrue(result is UseCaseResult.Success)
        coVerify(exactly = 1) { categoryRepository.addCategory("Subscriptions", "other", 0xFF000000) }
    }
}
