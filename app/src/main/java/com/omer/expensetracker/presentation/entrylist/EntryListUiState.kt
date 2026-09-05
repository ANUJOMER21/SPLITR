package com.omer.expensetracker.presentation.entrylist

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryFilter

data class EntrySection(
    val label: String,
    val entries: List<Entry>
)

data class EntryListUiState(
    val filter: EntryFilter = EntryFilter(),
    val sections: List<EntrySection> = emptyList(),
    val categoriesById: Map<String, Category> = emptyMap(),
    val filterableCategories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val pendingDeleteEntryId: String? = null,
    val lastDeletedEntryId: String? = null
)
