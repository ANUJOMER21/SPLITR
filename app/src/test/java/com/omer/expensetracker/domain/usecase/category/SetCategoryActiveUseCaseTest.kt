package com.omer.expensetracker.domain.usecase.category

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

class SetCategoryActiveUseCaseTest {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var useCase: SetCategoryActiveUseCase

    @Before
    fun setUp() {
        categoryRepository = mockk()
        coEvery { categoryRepository.setCategoryActive(any(), any()) } returns Unit
        useCase = SetCategoryActiveUseCase(categoryRepository)
    }

    @Test
    fun `blocks hiding the Other category`() = runTest {
        val result = useCase(OTHER_CATEGORY_ID, isActive = false)
        assertTrue(result is UseCaseResult.Failure)
        coVerify(exactly = 0) { categoryRepository.setCategoryActive(any(), any()) }
    }

    @Test
    fun `allows hiding a default category`() = runTest {
        val result = useCase("category_food", isActive = false)
        assertTrue(result is UseCaseResult.Success)
        coVerify(exactly = 1) { categoryRepository.setCategoryActive("category_food", false) }
    }

    @Test
    fun `allows re-activating the Other category`() = runTest {
        val result = useCase(OTHER_CATEGORY_ID, isActive = true)
        assertTrue(result is UseCaseResult.Success)
    }
}
