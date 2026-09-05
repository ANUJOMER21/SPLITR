package com.omer.expensetracker.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.usecase.category.GetAllCategoriesUseCase
import com.omer.expensetracker.domain.usecase.recurring.DeleteRecurringRuleUseCase
import com.omer.expensetracker.domain.usecase.recurring.GetRecurringRulesUseCase
import com.omer.expensetracker.domain.usecase.recurring.SetRecurringRulePausedUseCase
import com.omer.expensetracker.domain.usecase.recurring.SkipNextOccurrenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringRulesUiState(
    val rules: List<RecurringRule> = emptyList(),
    val categoriesById: Map<String, Category> = emptyMap(),
    val pendingDeleteId: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class RecurringRulesViewModel @Inject constructor(
    getRecurringRulesUseCase: GetRecurringRulesUseCase,
    getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val setRecurringRulePausedUseCase: SetRecurringRulePausedUseCase,
    private val skipNextOccurrenceUseCase: SkipNextOccurrenceUseCase,
    private val deleteRecurringRuleUseCase: DeleteRecurringRuleUseCase
) : ViewModel() {

    private val pendingDeleteId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RecurringRulesUiState> = combine(
        getRecurringRulesUseCase(), getAllCategoriesUseCase(), pendingDeleteId
    ) { rules, categories, pending ->
        RecurringRulesUiState(
            rules = rules,
            categoriesById = categories.associateBy { it.id },
            pendingDeleteId = pending,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecurringRulesUiState())

    fun togglePaused(id: String, isPaused: Boolean) {
        viewModelScope.launch { setRecurringRulePausedUseCase(id, isPaused) }
    }

    fun skipNext(id: String) {
        viewModelScope.launch { skipNextOccurrenceUseCase(id) }
    }

    fun requestDelete(id: String) = pendingDeleteId.update { id }
    fun cancelDelete() = pendingDeleteId.update { null }
    fun confirmDelete() {
        val id = pendingDeleteId.value ?: return
        viewModelScope.launch {
            deleteRecurringRuleUseCase(id)
            pendingDeleteId.value = null
        }
    }
}
