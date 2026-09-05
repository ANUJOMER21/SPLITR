package com.omer.expensetracker.presentation.entrylist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.usecase.DeleteEntryUseCase
import com.omer.expensetracker.domain.usecase.GetFilteredEntriesUseCase
import com.omer.expensetracker.domain.usecase.RestoreEntryUseCase
import com.omer.expensetracker.domain.usecase.category.GetAllCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EntryListViewModel @Inject constructor(
    private val getFilteredEntriesUseCase: GetFilteredEntriesUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val restoreEntryUseCase: RestoreEntryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialCategoryId: String? = savedStateHandle.get<String>("categoryId")?.takeIf { it.isNotBlank() }

    private val filterFlow = MutableStateFlow(
        EntryFilter(categoryIds = setOfNotNull(initialCategoryId))
    )
    private val transientFlow = MutableStateFlow(TransientState())

    val uiState: StateFlow<EntryListUiState> = combine(
        filterFlow.flatMapLatest { filter -> getFilteredEntriesUseCase(filter).map { filter to it } },
        getAllCategoriesUseCase(),
        transientFlow
    ) { (filter, entries), categories, transient ->
        val categoriesById = categories.associateBy { it.id }
        EntryListUiState(
            filter = filter,
            sections = groupEntriesByDate(entries),
            categoriesById = categoriesById,
            filterableCategories = categories.filter { it.isActive },
            isLoading = false,
            pendingDeleteEntryId = transient.pendingDeleteEntryId,
            lastDeletedEntryId = transient.lastDeletedEntryId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EntryListUiState())

    fun updateFilter(filter: EntryFilter) {
        filterFlow.value = filter
    }

    fun requestDelete(entryId: String) {
        transientFlow.update { it.copy(pendingDeleteEntryId = entryId) }
    }

    fun cancelDelete() {
        transientFlow.update { it.copy(pendingDeleteEntryId = null) }
    }

    fun confirmDelete() {
        val id = transientFlow.value.pendingDeleteEntryId ?: return
        viewModelScope.launch {
            deleteEntryUseCase(id)
            transientFlow.update { it.copy(pendingDeleteEntryId = null, lastDeletedEntryId = id) }
        }
    }

    fun undoDelete() {
        val id = transientFlow.value.lastDeletedEntryId ?: return
        viewModelScope.launch {
            restoreEntryUseCase(id)
            transientFlow.update { it.copy(lastDeletedEntryId = null) }
        }
    }

    fun consumeUndoPrompt() {
        transientFlow.update { it.copy(lastDeletedEntryId = null) }
    }

    private data class TransientState(
        val pendingDeleteEntryId: String? = null,
        val lastDeletedEntryId: String? = null
    )
}
