package com.omer.expensetracker.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.usecase.category.GetAllCategoriesUseCase
import com.omer.expensetracker.domain.usecase.reminder.DeleteBillReminderUseCase
import com.omer.expensetracker.domain.usecase.reminder.GetBillRemindersUseCase
import com.omer.expensetracker.domain.usecase.reminder.MarkReminderPaidUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillRemindersUiState(
    val reminders: List<BillReminder> = emptyList(),
    val categoriesById: Map<String, Category> = emptyMap(),
    val pendingDeleteId: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class BillRemindersViewModel @Inject constructor(
    getBillRemindersUseCase: GetBillRemindersUseCase,
    getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val markReminderPaidUseCase: MarkReminderPaidUseCase,
    private val deleteBillReminderUseCase: DeleteBillReminderUseCase
) : ViewModel() {

    private val pendingDeleteId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BillRemindersUiState> = combine(
        getBillRemindersUseCase(), getAllCategoriesUseCase(), pendingDeleteId
    ) { reminders, categories, pending ->
        BillRemindersUiState(
            reminders = reminders,
            categoriesById = categories.associateBy { it.id },
            pendingDeleteId = pending,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BillRemindersUiState())

    fun markPaid(id: String, autoCreateEntry: Boolean) {
        viewModelScope.launch { markReminderPaidUseCase(id, autoCreateEntry) }
    }

    fun requestDelete(id: String) = pendingDeleteId.update { id }
    fun cancelDelete() = pendingDeleteId.update { null }
    fun confirmDelete() {
        val id = pendingDeleteId.value ?: return
        viewModelScope.launch {
            deleteBillReminderUseCase(id)
            pendingDeleteId.value = null
        }
    }
}
