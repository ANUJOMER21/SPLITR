package com.omer.expensetracker.presentation.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.usecase.budget.GetBudgetForCategoryUseCase
import com.omer.expensetracker.domain.usecase.budget.UpsertBudgetUseCase
import com.omer.expensetracker.domain.usecase.category.GetActiveCategoriesUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.presentation.util.parseAmountToMinorUnits
import com.omer.expensetracker.presentation.util.toAmountInputString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

const val OVERALL_BUDGET_SENTINEL = "OVERALL"

data class AddEditBudgetUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val isOverallSelected: Boolean = false,
    val amountText: String = "",
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class AddEditBudgetViewModel @Inject constructor(
    private val upsertBudgetUseCase: UpsertBudgetUseCase,
    private val getBudgetForCategoryUseCase: GetBudgetForCategoryUseCase,
    getActiveCategoriesUseCase: GetActiveCategoriesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialArg: String? = savedStateHandle.get<String>("categoryId")?.takeIf { it.isNotBlank() }

    private val selectedCategoryId = MutableStateFlow(
        initialArg?.takeIf { it != OVERALL_BUDGET_SENTINEL }
    )
    private val isOverallSelected = MutableStateFlow(initialArg == OVERALL_BUDGET_SENTINEL)
    private val amountText = MutableStateFlow("")
    private val errorFlow = MutableStateFlow<String?>(null)
    private val savedFlow = MutableStateFlow(false)
    private val loadingFlow = MutableStateFlow(initialArg != null)

    init {
        if (initialArg != null) {
            viewModelScope.launch {
                val categoryId = initialArg.takeIf { it != OVERALL_BUDGET_SENTINEL }
                val existing = getBudgetForCategoryUseCase(categoryId)
                if (existing != null) {
                    amountText.value = existing.monthlyLimitMinor.toAmountInputString()
                }
                loadingFlow.value = false
            }
        }
    }

    val uiState: StateFlow<AddEditBudgetUiState> = combine(
        getActiveCategoriesUseCase(), selectedCategoryId, isOverallSelected, amountText, errorFlow, savedFlow, loadingFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AddEditBudgetUiState(
            categories = values[0] as List<Category>,
            selectedCategoryId = values[1] as String?,
            isOverallSelected = values[2] as Boolean,
            amountText = values[3] as String,
            errorMessage = values[4] as String?,
            isSaved = values[5] as Boolean,
            isLoading = values[6] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditBudgetUiState())

    fun selectCategory(id: String) {
        selectedCategoryId.value = id
        isOverallSelected.value = false
        errorFlow.value = null
        reloadExistingLimit(id)
    }

    fun selectOverall() {
        isOverallSelected.value = true
        selectedCategoryId.value = null
        errorFlow.value = null
        reloadExistingLimit(null)
    }

    /** Prefills the amount if this category already has a budget — but never clobbers
     * whatever the user has already typed just by switching which category is selected. */
    private fun reloadExistingLimit(categoryId: String?) {
        viewModelScope.launch {
            val existing = getBudgetForCategoryUseCase(categoryId)
            if (existing != null) {
                amountText.value = existing.monthlyLimitMinor.toAmountInputString()
            }
        }
    }

    fun onAmountChange(value: String) {
        amountText.value = value
        errorFlow.value = null
    }

    fun save() {
        if (!isOverallSelected.value && selectedCategoryId.value == null) {
            errorFlow.value = "Choose a category or Overall"
            return
        }
        val amountMinor = amountText.value.parseAmountToMinorUnits()
        if (amountMinor == null) {
            errorFlow.value = "Enter a limit greater than zero"
            return
        }
        viewModelScope.launch {
            val categoryId = if (isOverallSelected.value) null else selectedCategoryId.value
            when (val result = upsertBudgetUseCase(categoryId, amountMinor)) {
                is UseCaseResult.Success -> savedFlow.value = true
                is UseCaseResult.Failure -> errorFlow.value = result.message
            }
        }
    }
}
