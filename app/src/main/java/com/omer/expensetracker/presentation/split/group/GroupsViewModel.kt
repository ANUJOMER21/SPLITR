package com.omer.expensetracker.presentation.split.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.FriendGroup
import com.omer.expensetracker.domain.repository.split.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class GroupsUiState(val groups: List<FriendGroup> = emptyList(), val isLoading: Boolean = true)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    groupRepository: GroupRepository
) : ViewModel() {

    val uiState: StateFlow<GroupsUiState> = groupRepository.observeActiveGroups()
        .map { GroupsUiState(groups = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupsUiState())
}
