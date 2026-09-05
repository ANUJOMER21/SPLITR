package com.omer.expensetracker.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.CategoryWithTotal
import com.omer.expensetracker.domain.usecase.category.DeleteCategoryUseCase
import com.omer.expensetracker.domain.usecase.category.GetCategoriesWithTotalsUseCase
import com.omer.expensetracker.domain.usecase.category.SetCategoryActiveUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageCategoriesUiState(
    val categories: List<CategoryWithTotal> = emptyList(),
    val pendingDeleteCategoryId: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

private data class TransientState(
    val pendingDeleteCategoryId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    getCategoriesWithTotalsUseCase: GetCategoriesWithTotalsUseCase,
    private val setCategoryActiveUseCase: SetCategoryActiveUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val transientFlow = MutableStateFlow(TransientState())

    val uiState: StateFlow<ManageCategoriesUiState> = combine(
        getCategoriesWithTotalsUseCase(), transientFlow
    ) { categories, transient ->
        ManageCategoriesUiState(
            categories = categories,
            pendingDeleteCategoryId = transient.pendingDeleteCategoryId,
            errorMessage = transient.errorMessage,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ManageCategoriesUiState())

    fun setActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            val result = setCategoryActiveUseCase(id, isActive)
            if (result is UseCaseResult.Failure) {
                transientFlow.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun requestDelete(id: String) {
        transientFlow.update { it.copy(pendingDeleteCategoryId = id) }
    }

    fun cancelDelete() {
        transientFlow.update { it.copy(pendingDeleteCategoryId = null) }
    }

    fun confirmDelete() {
        val id = transientFlow.value.pendingDeleteCategoryId ?: return
        viewModelScope.launch {
            val result = deleteCategoryUseCase(id)
            transientFlow.value = when (result) {
                is UseCaseResult.Success -> TransientState()
                is UseCaseResult.Failure -> TransientState(errorMessage = result.message)
            }
        }
    }

    fun consumeError() {
        transientFlow.update { it.copy(errorMessage = null) }
    }
}
