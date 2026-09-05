package com.omer.expensetracker.presentation.split.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.usecase.split.DebtSimplifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SimplifiedPaymentRow(val fromName: String, val toName: String, val amountMinor: Long)

data class SimplifyDebtsUiState(
    val showSimplified: Boolean = true,
    val rawBalances: List<Pair<String, Long>> = emptyList(),
    val simplifiedPayments: List<SimplifiedPaymentRow> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SimplifyDebtsViewModel @Inject constructor(
    friendRepository: FriendRepository,
    balanceRepository: BalanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = requireNotNull(savedStateHandle["groupId"])
    private val showSimplified = MutableStateFlow(true)

    val uiState: StateFlow<SimplifyDebtsUiState> = combine(
        showSimplified,
        balanceRepository.observeGroupBalances(groupId),
        friendRepository.observeFriends()
    ) { simplified, balances, friends ->
        val nameById = friends.associate { it.id to (if (it.isYou) "You" else it.name) }
        val net = DebtSimplifier.toCreditorDebtorMap(balances.associate { it.friendId to it.netMinor })

        val payments = DebtSimplifier.simplify(net).map {
            SimplifiedPaymentRow(
                fromName = nameById[it.fromFriendId] ?: "Someone",
                toName = nameById[it.toFriendId] ?: "Someone",
                amountMinor = it.amountMinor
            )
        }
        SimplifyDebtsUiState(
            showSimplified = simplified,
            rawBalances = balances.filter { it.netMinor != 0L }.map { (nameById[it.friendId] ?: "Someone") to it.netMinor },
            simplifiedPayments = payments,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SimplifyDebtsUiState())

    fun toggleView() { showSimplified.value = !showSimplified.value }
}
