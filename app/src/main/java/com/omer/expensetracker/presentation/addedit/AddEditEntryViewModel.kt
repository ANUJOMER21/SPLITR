package com.omer.expensetracker.presentation.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.usecase.AddExpenseUseCase
import com.omer.expensetracker.domain.usecase.AddIncomeUseCase
import com.omer.expensetracker.domain.usecase.GetEntryUseCase
import com.omer.expensetracker.domain.usecase.UpdateEntryUseCase
import com.omer.expensetracker.domain.usecase.category.GetActiveCategoriesUseCase
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

private const val KEY_AMOUNT = "amountText"
private const val KEY_CATEGORY = "categoryId"
private const val KEY_DATE = "dateEpochDay"
private const val KEY_TYPE = "type"
private const val KEY_CREATED_AT = "createdAt"
private const val KEY_INITIALIZED = "initialized"

private data class FormFields(
    val amountText: String,
    val categoryId: String?,
    val dateEpochDay: Long,
    val type: String
)

@HiltViewModel
class AddEditEntryViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val addIncomeUseCase: AddIncomeUseCase,
    private val updateEntryUseCase: UpdateEntryUseCase,
    private val getEntryUseCase: GetEntryUseCase,
    getActiveCategoriesUseCase: GetActiveCategoriesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: String = savedStateHandle["entryId"] ?: NEW_ID
    private val isEditing = entryId != NEW_ID

    private val amountText = savedStateHandle.getStateFlow(KEY_AMOUNT, "")
    private val categoryId = savedStateHandle.getStateFlow<String?>(KEY_CATEGORY, null)
    private val dateEpochDay = savedStateHandle.getStateFlow(KEY_DATE, LocalDate.now().toEpochDay())
    private val type = savedStateHandle.getStateFlow(
        KEY_TYPE,
        savedStateHandle.get<String>("type") ?: EntryType.EXPENSE.name
    )

    private val errorFlow = MutableStateFlow<String?>(null)
    private val savedFlow = MutableStateFlow(false)
    private val loadingFlow = MutableStateFlow(isEditing)

    private val formFlow = combine(amountText, categoryId, dateEpochDay, type) { amt, cat, day, t ->
        FormFields(amt, cat, day, t)
    }

    init {
        if (savedStateHandle.get<Boolean>(KEY_INITIALIZED) != true) {
            savedStateHandle[KEY_INITIALIZED] = true
            if (isEditing) {
                viewModelScope.launch {
                    val entry = getEntryUseCase(entryId)
                    if (entry != null) applyLoadedEntry(entry)
                    loadingFlow.value = false
                }
            } else {
                savedStateHandle[KEY_DATE] = LocalDate.now().toEpochDay()
            }
        } else {
            loadingFlow.value = false
        }
    }

    val uiState: StateFlow<AddEditEntryUiState> = combine(
        formFlow, getActiveCategoriesUseCase(), errorFlow, savedFlow, loadingFlow
    ) { form, categories, error, saved, loading ->
        AddEditEntryUiState(
            isEditing = isEditing,
            type = EntryType.valueOf(form.type),
            amountText = form.amountText,
            categoryId = form.categoryId,
            date = LocalDate.ofEpochDay(form.dateEpochDay),
            categories = categories,
            errorMessage = error,
            isLoading = loading,
            isSaved = saved
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditEntryUiState(isEditing = isEditing))

    private fun applyLoadedEntry(entry: Entry) {
        savedStateHandle[KEY_AMOUNT] = entry.amountMinor.toAmountInputString()
        savedStateHandle[KEY_CATEGORY] = entry.categoryId
        savedStateHandle[KEY_DATE] = entry.date.toEpochDay()
        savedStateHandle[KEY_TYPE] = entry.type.name
        savedStateHandle[KEY_CREATED_AT] = entry.createdAt
    }

    fun onAmountChange(value: String) {
        savedStateHandle[KEY_AMOUNT] = value
        errorFlow.value = null
    }

    fun onCategorySelect(id: String) {
        savedStateHandle[KEY_CATEGORY] = id
        errorFlow.value = null
    }

    fun onDateChange(date: LocalDate) {
        savedStateHandle[KEY_DATE] = date.toEpochDay()
    }

    fun save() {
        val form = FormFields(amountText.value, categoryId.value, dateEpochDay.value, type.value)
        val currentType = EntryType.valueOf(form.type)
        val amountMinor = form.amountText.parseAmountToMinorUnits()
        if (amountMinor == null) {
            errorFlow.value = "Enter an amount greater than zero"
            return
        }
        if (currentType == EntryType.EXPENSE && form.categoryId.isNullOrBlank()) {
            errorFlow.value = "Select a category"
            return
        }
        val date = LocalDate.ofEpochDay(form.dateEpochDay)

        viewModelScope.launch {
            val result: UseCaseResult<Unit> = if (isEditing) {
                updateEntryUseCase(
                    Entry(
                        id = entryId,
                        type = currentType,
                        amountMinor = amountMinor,
                        categoryId = if (currentType == EntryType.EXPENSE) form.categoryId else null,
                        date = date,
                        createdAt = savedStateHandle[KEY_CREATED_AT] ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else if (currentType == EntryType.EXPENSE) {
                addExpenseUseCase(amountMinor, form.categoryId, date).toUnitResult()
            } else {
                addIncomeUseCase(amountMinor, date).toUnitResult()
            }

            when (result) {
                is UseCaseResult.Success -> savedFlow.value = true
                is UseCaseResult.Failure -> errorFlow.value = result.message
            }
        }
    }

    private fun <T> UseCaseResult<T>.toUnitResult(): UseCaseResult<Unit> = when (this) {
        is UseCaseResult.Success -> UseCaseResult.Success(Unit)
        is UseCaseResult.Failure -> this
    }
}
