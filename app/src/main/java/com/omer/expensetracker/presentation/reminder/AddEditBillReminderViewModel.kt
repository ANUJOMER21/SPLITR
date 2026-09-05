package com.omer.expensetracker.presentation.reminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.usecase.reminder.GetBillReminderUseCase
import com.omer.expensetracker.domain.usecase.category.GetActiveCategoriesUseCase
import com.omer.expensetracker.domain.usecase.reminder.AddBillReminderUseCase
import com.omer.expensetracker.domain.usecase.reminder.UpdateBillReminderUseCase
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

data class AddEditBillReminderUiState(
    val isEditing: Boolean = false,
    val label: String = "",
    val amountText: String = "",
    val categoryId: String? = null,
    val dueDate: LocalDate = LocalDate.now().plusDays(7),
    val leadDaysText: String = "3",
    val repeats: Boolean = false,
    val repeatUnit: RecurrenceUnit = RecurrenceUnit.MONTH,
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditBillReminderViewModel @Inject constructor(
    private val addBillReminderUseCase: AddBillReminderUseCase,
    private val updateBillReminderUseCase: UpdateBillReminderUseCase,
    private val getBillReminderUseCase: GetBillReminderUseCase,
    getActiveCategoriesUseCase: GetActiveCategoriesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: String = savedStateHandle["reminderId"] ?: NEW_ID
    private val isEditing = reminderId != NEW_ID

    private val label = MutableStateFlow("")
    private val amountText = MutableStateFlow("")
    private val categoryId = MutableStateFlow<String?>(null)
    private val dueDate = MutableStateFlow(LocalDate.now().plusDays(7))
    private val leadDaysText = MutableStateFlow("3")
    private val repeats = MutableStateFlow(false)
    private val repeatUnit = MutableStateFlow(RecurrenceUnit.MONTH)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val savedFlow = MutableStateFlow(false)
    private val loadingFlow = MutableStateFlow(isEditing)

    init {
        if (isEditing) {
            viewModelScope.launch {
                getBillReminderUseCase(reminderId)?.let { reminder ->
                    label.value = reminder.label
                    amountText.value = reminder.estimatedAmountMinor?.toAmountInputString() ?: ""
                    categoryId.value = reminder.categoryId
                    dueDate.value = reminder.dueDate
                    leadDaysText.value = reminder.leadDays.toString()
                    repeats.value = reminder.isRepeating
                    reminder.repeatUnit?.let { repeatUnit.value = it }
                }
                loadingFlow.value = false
            }
        }
    }

    private data class Part1(val label: String, val amountText: String, val categoryId: String?, val dueDate: LocalDate)
    private data class Part2(val leadDaysText: String, val repeats: Boolean, val repeatUnit: RecurrenceUnit)

    private val part1 = combine(label, amountText, categoryId, dueDate) { l, a, c, d -> Part1(l, a, c, d) }
    private val part2 = combine(leadDaysText, repeats, repeatUnit) { lead, rep, unit -> Part2(lead, rep, unit) }
    private val coreFlow = combine(part1, part2) { p1, p2 -> p1 to p2 }

    val uiState: StateFlow<AddEditBillReminderUiState> = combine(
        coreFlow, getActiveCategoriesUseCase(), errorFlow, savedFlow, loadingFlow
    ) { (p1, p2), categories, error, saved, loading ->
        AddEditBillReminderUiState(
            isEditing = isEditing,
            label = p1.label,
            amountText = p1.amountText,
            categoryId = p1.categoryId,
            dueDate = p1.dueDate,
            leadDaysText = p2.leadDaysText,
            repeats = p2.repeats,
            repeatUnit = p2.repeatUnit,
            categories = categories,
            errorMessage = error,
            isSaved = saved,
            isLoading = loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditBillReminderUiState(isEditing = isEditing))

    fun onLabelChange(value: String) {
        label.value = value
        errorFlow.value = null
    }

    fun onAmountChange(value: String) {
        amountText.value = value
    }

    fun onCategorySelect(id: String?) {
        categoryId.value = id
    }

    fun onDueDateChange(date: LocalDate) {
        dueDate.value = date
    }

    fun onLeadDaysChange(value: String) {
        leadDaysText.value = value.filter { it.isDigit() }
    }

    fun onRepeatsChange(value: Boolean) {
        repeats.value = value
    }

    fun onRepeatUnitChange(unit: RecurrenceUnit) {
        repeatUnit.value = unit
    }

    fun save() {
        val amountMinor = amountText.value.takeIf { it.isNotBlank() }?.parseAmountToMinorUnits()
        val leadDays = leadDaysText.value.toIntOrNull() ?: 0
        val effectiveRepeatUnit = if (repeats.value) repeatUnit.value else null
        val effectiveRepeatCount = if (repeats.value) 1 else null

        viewModelScope.launch {
            val result: UseCaseResult<Unit> = if (isEditing) {
                getBillReminderUseCase(reminderId)?.let { existing ->
                    val updated = existing.copy(
                        label = label.value,
                        estimatedAmountMinor = amountMinor,
                        categoryId = categoryId.value,
                        dueDate = dueDate.value,
                        leadDays = leadDays,
                        repeatUnit = effectiveRepeatUnit,
                        repeatIntervalCount = effectiveRepeatCount
                    )
                    updateBillReminderUseCase(updated)
                } ?: UseCaseResult.Failure("Reminder not found")
            } else {
                addBillReminderUseCase(
                    label.value, amountMinor, categoryId.value, dueDate.value, leadDays,
                    effectiveRepeatUnit, effectiveRepeatCount
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
