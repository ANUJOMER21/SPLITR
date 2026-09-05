package com.omer.expensetracker.presentation.recurring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.usecase.category.GetActiveCategoriesUseCase
import com.omer.expensetracker.domain.usecase.recurring.AddRecurringRuleUseCase
import com.omer.expensetracker.domain.usecase.recurring.GetRecurringRuleUseCase
import com.omer.expensetracker.domain.usecase.recurring.UpdateRecurringRuleUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.presentation.navigation.NEW_ID
import com.omer.expensetracker.presentation.util.parseAmountToMinorUnits
import com.omer.expensetracker.presentation.util.toAmountInputString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditRecurringRuleUiState(
    val isEditing: Boolean = false,
    val type: EntryType = EntryType.EXPENSE,
    val amountText: String = "",
    val categoryId: String? = null,
    val unit: RecurrenceUnit = RecurrenceUnit.MONTH,
    val intervalCountText: String = "1",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditRecurringRuleViewModel @Inject constructor(
    private val addRecurringRuleUseCase: AddRecurringRuleUseCase,
    private val updateRecurringRuleUseCase: UpdateRecurringRuleUseCase,
    private val getRecurringRuleUseCase: GetRecurringRuleUseCase,
    getActiveCategoriesUseCase: GetActiveCategoriesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ruleId: String = savedStateHandle["ruleId"] ?: NEW_ID
    private val isEditing = ruleId != NEW_ID

    private val type = MutableStateFlow(EntryType.EXPENSE)
    private val amountText = MutableStateFlow("")
    private val categoryId = MutableStateFlow<String?>(null)
    private val unit = MutableStateFlow(RecurrenceUnit.MONTH)
    private val intervalCountText = MutableStateFlow("1")
    private val startDate = MutableStateFlow(LocalDate.now())
    private val endDate = MutableStateFlow<LocalDate?>(null)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val savedFlow = MutableStateFlow(false)
    private val loadingFlow = MutableStateFlow(isEditing)

    init {
        if (isEditing) {
            viewModelScope.launch {
                getRecurringRuleUseCase(ruleId)?.let { rule ->
                    type.value = rule.type
                    amountText.value = rule.amountMinor.toAmountInputString()
                    categoryId.value = rule.categoryId
                    unit.value = rule.unit
                    intervalCountText.value = rule.intervalCount.toString()
                    startDate.value = rule.startDate
                    endDate.value = rule.endDate
                }
                loadingFlow.value = false
            }
        }
    }

    private data class FormPart(val type: EntryType, val amountText: String, val categoryId: String?, val unit: RecurrenceUnit)
    private data class DatePart(val intervalCountText: String, val startDate: LocalDate, val endDate: LocalDate?)

    private val formFlow = combine(type, amountText, categoryId, unit) { t, amt, cat, u -> FormPart(t, amt, cat, u) }
    private val dateFlow = combine(intervalCountText, startDate, endDate) { count, s, e -> DatePart(count, s, e) }
    private val coreFlow = combine(formFlow, dateFlow) { form, dates -> form to dates }

    val uiState: StateFlow<AddEditRecurringRuleUiState> = combine(
        coreFlow, getActiveCategoriesUseCase(), errorFlow, savedFlow, loadingFlow
    ) { (form, dates), categories, error, saved, loading ->
        AddEditRecurringRuleUiState(
            isEditing = isEditing,
            type = form.type,
            amountText = form.amountText,
            categoryId = form.categoryId,
            unit = form.unit,
            intervalCountText = dates.intervalCountText,
            startDate = dates.startDate,
            endDate = dates.endDate,
            categories = categories,
            errorMessage = error,
            isSaved = saved,
            isLoading = loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditRecurringRuleUiState(isEditing = isEditing))

    fun onTypeChange(newType: EntryType) {
        type.value = newType
        errorFlow.value = null
    }

    fun onAmountChange(value: String) {
        amountText.value = value
        errorFlow.value = null
    }

    fun onCategorySelect(id: String) {
        categoryId.value = id
        errorFlow.value = null
    }

    fun onUnitChange(newUnit: RecurrenceUnit) {
        unit.value = newUnit
    }

    fun onIntervalCountChange(value: String) {
        intervalCountText.value = value.filter { it.isDigit() }
    }

    fun onStartDateChange(date: LocalDate) {
        startDate.value = date
    }

    fun onEndDateChange(date: LocalDate?) {
        endDate.value = date
    }

    fun onHasEndDateToggle(enabled: Boolean) {
        endDate.value = if (enabled) (endDate.value ?: startDate.value) else null
    }

    fun save() {
        val amountMinor = amountText.value.parseAmountToMinorUnits()
        if (amountMinor == null) {
            errorFlow.value = "Enter an amount greater than zero"
            return
        }
        val intervalCount = intervalCountText.value.toIntOrNull() ?: 0
        viewModelScope.launch {
            val result: UseCaseResult<Unit> = if (isEditing) {
                getRecurringRuleUseCase(ruleId)?.let { existing ->
                    updateRecurringRuleUseCase(
                        existing.copy(
                            type = type.value,
                            amountMinor = amountMinor,
                            categoryId = if (type.value == EntryType.EXPENSE) categoryId.value else null,
                            unit = unit.value,
                            intervalCount = intervalCount,
                            startDate = startDate.value,
                            endDate = endDate.value
                        )
                    )
                } ?: UseCaseResult.Failure("Rule not found")
            } else {
                addRecurringRuleUseCase(
                    type.value, amountMinor,
                    if (type.value == EntryType.EXPENSE) categoryId.value else null,
                    unit.value, intervalCount, startDate.value, endDate.value
                ).let {
                    when (it) {
                        is UseCaseResult.Success -> UseCaseResult.Success(Unit)
                        is UseCaseResult.Failure -> it
                    }
                }
            }
            when (result) {
                is UseCaseResult.Success -> savedFlow.value = true
                is UseCaseResult.Failure -> errorFlow.value = result.message
            }
        }
    }
}
