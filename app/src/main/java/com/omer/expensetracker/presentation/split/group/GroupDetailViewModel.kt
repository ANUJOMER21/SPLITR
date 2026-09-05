package com.omer.expensetracker.presentation.split.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.ActivityLogEntry
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.FriendGroup
import com.omer.expensetracker.domain.model.split.SharedExpense
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.ActivityLogRepository
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.split.GroupRepository
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GroupDetailTab { EXPENSES, BALANCES, ACTIVITY }

data class MemberBalance(val friend: Friend, val netMinor: Long)

data class GroupDetailUiState(
    val group: FriendGroup? = null,
    val tab: GroupDetailTab = GroupDetailTab.EXPENSES,
    val members: List<MemberBalance> = emptyList(),
    val expenses: List<SharedExpense> = emptyList(),
    val activity: List<ActivityLogEntry> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    friendRepository: FriendRepository,
    balanceRepository: BalanceRepository,
    sharedExpenseRepository: SharedExpenseRepository,
    activityLogRepository: ActivityLogRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: String = requireNotNull(savedStateHandle["groupId"])
    private val group = MutableStateFlow<FriendGroup?>(null)
    private val tab = MutableStateFlow(GroupDetailTab.EXPENSES)

    init {
        viewModelScope.launch { group.value = groupRepository.getGroup(groupId) }
    }

    val uiState: StateFlow<GroupDetailUiState> = combine(
        group, tab,
        groupRepository.observeMemberIds(groupId),
        friendRepository.observeFriends(),
        balanceRepository.observeGroupBalances(groupId),
        sharedExpenseRepository.observeForGroup(groupId),
        activityLogRepository.observeForGroup(groupId)
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val g = values[0] as FriendGroup?
        val t = values[1] as GroupDetailTab
        val memberIds = (values[2] as List<String>).toSet()
        val friends = values[3] as List<Friend>
        val balances = (values[4] as List<com.omer.expensetracker.domain.model.split.Balance>).associateBy { it.friendId }
        val expenses = values[5] as List<SharedExpense>
        val activity = values[6] as List<ActivityLogEntry>

        GroupDetailUiState(
            group = g,
            tab = t,
            members = friends.filter { it.id in memberIds && it.id != YOU_FRIEND_ID }
                .map { MemberBalance(it, balances[it.id]?.netMinor ?: 0L) },
            expenses = expenses,
            activity = activity,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupDetailUiState())

    fun selectTab(newTab: GroupDetailTab) { tab.value = newTab }
}
