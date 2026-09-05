package com.omer.expensetracker.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.SavingsGoalDetail
import com.omer.expensetracker.domain.usecase.goal.DeleteSavingsGoalUseCase
import com.omer.expensetracker.domain.usecase.goal.GetGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsUiState(
    val goals: List<SavingsGoalDetail> = emptyList(),
    val pendingDeleteId: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    getGoalsUseCase: GetGoalsUseCase,
    private val deleteSavingsGoalUseCase: DeleteSavingsGoalUseCase
) : ViewModel() {

    private val pendingDeleteId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(getGoalsUseCase(), pendingDeleteId) { goals, pending ->
        GoalsUiState(goals = goals, pendingDeleteId = pending, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    fun requestDelete(id: String) = pendingDeleteId.update { id }
    fun cancelDelete() = pendingDeleteId.update { null }
    fun confirmDelete() {
        val id = pendingDeleteId.value ?: return
        viewModelScope.launch {
            deleteSavingsGoalUseCase(id)
            pendingDeleteId.value = null
        }
    }
}
