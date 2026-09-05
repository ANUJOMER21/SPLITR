package com.omer.expensetracker.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.usecase.goal.AddSavingsGoalUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.presentation.util.parseAmountToMinorUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddGoalUiState(
    val name: String = "",
    val amountText: String = "",
    val targetDate: LocalDate? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditGoalViewModel @Inject constructor(
    private val addSavingsGoalUseCase: AddSavingsGoalUseCase
) : ViewModel() {

    private val name = MutableStateFlow("")
    private val amountText = MutableStateFlow("")
    private val targetDate = MutableStateFlow<LocalDate?>(null)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val savedFlow = MutableStateFlow(false)

    val uiState: StateFlow<AddGoalUiState> = combine(
        name, amountText, targetDate, errorFlow, savedFlow
    ) { n, amt, date, error, saved ->
        AddGoalUiState(n, amt, date, error, saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddGoalUiState())

    fun onNameChange(value: String) {
        name.value = value
        errorFlow.value = null
    }

    fun onAmountChange(value: String) {
        amountText.value = value
        errorFlow.value = null
    }

    fun onTargetDateChange(date: LocalDate?) {
        targetDate.value = date
    }

    fun save() {
        val amountMinor = amountText.value.parseAmountToMinorUnits()
        if (amountMinor == null) {
            errorFlow.value = "Enter a target amount greater than zero"
            return
        }
        viewModelScope.launch {
            when (val result = addSavingsGoalUseCase(name.value, amountMinor, targetDate.value)) {
                is UseCaseResult.Success -> savedFlow.value = true
                is UseCaseResult.Failure -> errorFlow.value = result.message
            }
        }
    }
}
