package com.omer.expensetracker.presentation.addedit

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.EntryType
import java.time.LocalDate

data class AddEditEntryUiState(
    val isEditing: Boolean = false,
    val type: EntryType = EntryType.EXPENSE,
    val amountText: String = "",
    val categoryId: String? = null,
    val date: LocalDate = LocalDate.now(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)
