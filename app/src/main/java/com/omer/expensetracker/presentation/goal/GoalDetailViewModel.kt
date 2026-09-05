package com.omer.expensetracker.presentation.goal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.SavingsGoalDetail
import com.omer.expensetracker.domain.usecase.goal.DeleteContributionUseCase
import com.omer.expensetracker.domain.usecase.goal.GetGoalDetailUseCase
import com.omer.expensetracker.domain.usecase.goal.LogContributionUseCase
import com.omer.expensetracker.domain.usecase.goal.MarkGoalCompleteUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.notification.NotificationHelper
import com.omer.expensetracker.presentation.util.parseAmountToMinorUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GoalDetailUiState(
    val detail: SavingsGoalDetail? = null,
    val showAddContribution: Boolean = false,
    val contributionAmountText: String = "",
    val contributionDate: LocalDate = LocalDate.now(),
    val errorMessage: String? = null,
    val pendingDeleteContributionId: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val getGoalDetailUseCase: GetGoalDetailUseCase,
    private val logContributionUseCase: LogContributionUseCase,
    private val deleteContributionUseCase: DeleteContributionUseCase,
    private val markGoalCompleteUseCase: MarkGoalCompleteUseCase,
    private val notificationHelper: NotificationHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val goalId: String = requireNotNull(savedStateHandle["goalId"])

    private val showAddContribution = MutableStateFlow(false)
    private val contributionAmountText = MutableStateFlow("")
    private val contributionDate = MutableStateFlow(LocalDate.now())
    private val errorFlow = MutableStateFlow<String?>(null)
    private val pendingDeleteContributionId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GoalDetailUiState> = combine(
        getGoalDetailUseCase(goalId), showAddContribution, contributionAmountText,
        contributionDate, errorFlow, pendingDeleteContributionId
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        GoalDetailUiState(
            detail = values[0] as SavingsGoalDetail?,
            showAddContribution = values[1] as Boolean,
            contributionAmountText = values[2] as String,
            contributionDate = values[3] as LocalDate,
            errorMessage = values[4] as String?,
            pendingDeleteContributionId = values[5] as String?,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalDetailUiState())

    fun openAddContribution() {
        contributionAmountText.value = ""
        contributionDate.value = LocalDate.now()
        showAddContribution.value = true
    }

    fun dismissAddContribution() {
        showAddContribution.value = false
        errorFlow.value = null
    }

    fun onContributionAmountChange(value: String) {
        contributionAmountText.value = value
        errorFlow.value = null
    }

    fun onContributionDateChange(date: LocalDate) {
        contributionDate.value = date
    }

    fun saveContribution() {
        val amountMinor = contributionAmountText.value.parseAmountToMinorUnits()
        if (amountMinor == null) {
            errorFlow.value = "Enter an amount greater than zero"
            return
        }
        val wasCompleted = uiState.value.detail?.goal?.isCompleted == true
        viewModelScope.launch {
            when (val result = logContributionUseCase(goalId, amountMinor, contributionDate.value)) {
                is UseCaseResult.Success -> {
                    showAddContribution.value = false
                    val goalName = uiState.value.detail?.goal?.name ?: "Your goal"
                    if (!wasCompleted && getGoalDetailUseCase(goalId).first()?.goal?.isCompleted == true) {
                        notificationHelper.notifyGoal(
                            id = goalId.hashCode(),
                            title = "Goal reached!",
                            text = "\"$goalName\" hit its target — nice work."
                        )
                    }
                }
                is UseCaseResult.Failure -> errorFlow.value = result.message
            }
        }
    }

    fun requestDeleteContribution(id: String) {
        pendingDeleteContributionId.value = id
    }

    fun cancelDeleteContribution() {
        pendingDeleteContributionId.value = null
    }

    fun confirmDeleteContribution() {
        val id = pendingDeleteContributionId.value ?: return
        viewModelScope.launch {
            deleteContributionUseCase(id)
            pendingDeleteContributionId.value = null
        }
    }

    fun toggleCompleted(isCompleted: Boolean) {
        viewModelScope.launch { markGoalCompleteUseCase(goalId, isCompleted) }
    }
}
