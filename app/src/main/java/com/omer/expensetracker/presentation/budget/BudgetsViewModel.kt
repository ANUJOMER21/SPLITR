package com.omer.expensetracker.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.BudgetProgress
import com.omer.expensetracker.domain.usecase.budget.DeleteBudgetUseCase
import com.omer.expensetracker.domain.usecase.budget.GetBudgetsWithProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class BudgetsUiState(
    val month: YearMonth = YearMonth.now(),
    val budgets: List<BudgetProgress> = emptyList(),
    val pendingDeleteId: String? = null,
    val isLoading: Boolean = true
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val getBudgetsWithProgressUseCase: GetBudgetsWithProgressUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val pendingDeleteId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BudgetsUiState> = combine(
        month.flatMapLatest { getBudgetsWithProgressUseCase(it) },
        month,
        pendingDeleteId
    ) { budgets, m, pending ->
        BudgetsUiState(month = m, budgets = budgets, pendingDeleteId = pending, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetsUiState())

    fun previousMonth() = month.update { it.minusMonths(1) }
    fun nextMonth() = month.update { it.plusMonths(1) }

    fun requestDelete(id: String) {
        pendingDeleteId.value = id
    }

    fun cancelDelete() {
        pendingDeleteId.value = null
    }

    fun confirmDelete() {
        val id = pendingDeleteId.value ?: return
        viewModelScope.launch {
            deleteBudgetUseCase(id)
            pendingDeleteId.value = null
        }
    }
}
