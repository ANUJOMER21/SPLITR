package com.omer.expensetracker.presentation.split.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import com.omer.expensetracker.domain.repository.split.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FriendWithBalance(val friend: Friend, val netMinor: Long)

data class FriendsUiState(
    val friends: List<FriendWithBalance> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    friendRepository: FriendRepository,
    balanceRepository: BalanceRepository
) : ViewModel() {

    val uiState: StateFlow<FriendsUiState> = combine(
        friendRepository.observeFriends(),
        balanceRepository.observeAllOverallBalances()
    ) { friends, balances ->
        val balanceByFriendId = balances.associateBy { it.friendId }
        FriendsUiState(
            friends = friends.filter { !it.isYou }
                .map { f -> FriendWithBalance(f, balanceByFriendId[f.id]?.netMinor ?: 0L) },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FriendsUiState())
}
