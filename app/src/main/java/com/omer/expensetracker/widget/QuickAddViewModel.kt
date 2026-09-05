package com.omer.expensetracker.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.usecase.AddExpenseUseCase
import com.omer.expensetracker.domain.usecase.AddIncomeUseCase
import com.omer.expensetracker.domain.usecase.category.GetActiveCategoriesUseCase
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

data class QuickAddUiState(
    val type: EntryType = EntryType.EXPENSE,
    val amountText: String = "",
    val categoryId: String? = null,
    val date: LocalDate = LocalDate.now(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val addIncomeUseCase: AddIncomeUseCase,
    getActiveCategoriesUseCase: GetActiveCategoriesUseCase
) : ViewModel() {

    private val type = MutableStateFlow(EntryType.EXPENSE)
    private val amountText = MutableStateFlow("")
    private val categoryId = MutableStateFlow<String?>(null)
    private val date = MutableStateFlow(LocalDate.now())
    private val errorFlow = MutableStateFlow<String?>(null)
    private val savedFlow = MutableStateFlow(false)

    val uiState: StateFlow<QuickAddUiState> = combine(
        combine(type, amountText, categoryId, date) { t, amt, cat, d -> QuickForm(t, amt, cat, d) },
        getActiveCategoriesUseCase(),
        errorFlow,
        savedFlow
    ) { form, categories, error, saved ->
        QuickAddUiState(form.type, form.amountText, form.categoryId, form.date, categories, error, saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuickAddUiState(type = type.value))

    fun setType(newType: EntryType) {
        type.value = newType
    }

    fun onAmountChange(value: String) {
        amountText.value = value
        errorFlow.value = null
    }

    fun onCategorySelect(id: String) {
        categoryId.value = id
        errorFlow.value = null
    }

    fun onDateChange(newDate: LocalDate) {
        date.value = newDate
    }

    fun save() {
        val amountMinor = amountText.value.parseAmountToMinorUnits()
        if (amountMinor == null) {
            errorFlow.value = "Enter an amount greater than zero"
            return
        }
        if (type.value == EntryType.EXPENSE && categoryId.value.isNullOrBlank()) {
            errorFlow.value = "Select a category"
            return
        }
        viewModelScope.launch {
            val result = if (type.value == EntryType.EXPENSE) {
                addExpenseUseCase(amountMinor, categoryId.value, date.value)
            } else {
                addIncomeUseCase(amountMinor, date.value)
            }
            when (result) {
                is UseCaseResult.Success -> savedFlow.value = true
                is UseCaseResult.Failure -> errorFlow.value = result.message
            }
        }
    }

    private data class QuickForm(
        val type: EntryType,
        val amountText: String,
        val categoryId: String?,
        val date: LocalDate
    )
}
