package com.omer.expensetracker.domain.usecase.category

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.OTHER_CATEGORY_ID
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteCategoryUseCaseTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var useCase: DeleteCategoryUseCase

    private fun category(id: String, isDefault: Boolean) = Category(
        id = id,
        name = "Test",
        iconKey = "other",
        colorArgb = 0xFF000000,
        isDefault = isDefault,
        isActive = true,
        createdAt = 0L,
        updatedAt = 0L
    )

    @Before
    fun setUp() {
        categoryRepository = mockk()
        coEvery { categoryRepository.deleteCustomCategory(any()) } returns Unit
        useCase = DeleteCategoryUseCase(categoryRepository)
    }

    @Test
    fun `blocks deleting the Other category`() = runTest {
        val result = useCase(OTHER_CATEGORY_ID)
        assertTrue(result is UseCaseResult.Failure)
        coVerify(exactly = 0) { categoryRepository.deleteCustomCategory(any()) }
    }

    @Test
    fun `blocks deleting a default category`() = runTest {
        coEvery { categoryRepository.getCategory("cat_default") } returns category("cat_default", isDefault = true)

        val result = useCase("cat_default")

        assertTrue(result is UseCaseResult.Failure)
        coVerify(exactly = 0) { categoryRepository.deleteCustomCategory(any()) }
    }

    @Test
    fun `deletes a custom category and reassigns its entries`() = runTest {
        coEvery { categoryRepository.getCategory("cat_custom") } returns category("cat_custom", isDefault = false)

        val result = useCase("cat_custom")

        assertTrue(result is UseCaseResult.Success)
        coVerify(exactly = 1) { categoryRepository.deleteCustomCategory("cat_custom") }
    }

    @Test
    fun `fails when category does not exist`() = runTest {
        coEvery { categoryRepository.getCategory("missing") } returns null

        val result = useCase("missing")

        assertTrue(result is UseCaseResult.Failure)
    }
}
