package com.omer.expensetracker.presentation.split.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.FriendGroup
import com.omer.expensetracker.domain.repository.split.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class GroupWithMemberCount(val group: FriendGroup, val memberCount: Int)

data class GroupsUiState(val groups: List<GroupWithMemberCount> = emptyList(), val isLoading: Boolean = true)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    val uiState: StateFlow<GroupsUiState> = groupRepository.observeActiveGroups()
        .flatMapLatest { groups ->
            if (groups.isEmpty()) return@flatMapLatest flowOf(GroupsUiState(isLoading = false))
            combine(groups.map { g -> groupRepository.observeMemberIds(g.id).map { it.size } }) { counts ->
                GroupsUiState(
                    groups = groups.mapIndexed { i, g -> GroupWithMemberCount(g, counts[i]) }, // members already includes "You" — see AddGroupUseCase
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupsUiState())
}
