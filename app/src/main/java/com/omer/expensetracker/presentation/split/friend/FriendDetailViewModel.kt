package com.omer.expensetracker.presentation.split.friend

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.SharedExpense
import com.omer.expensetracker.domain.model.split.Settlement
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.split.SettlementRepository
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import com.omer.expensetracker.domain.repository.sync.ReminderRepository
import com.omer.expensetracker.domain.usecase.split.DeleteFriendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FriendLedgerEntry {
    val timestampMillis: Long
    data class ExpenseEntry(val expense: SharedExpense) : FriendLedgerEntry {
        override val timestampMillis get() = expense.date.toEpochDay() * 86_400_000L
    }
    data class SettlementEntry(val settlement: Settlement) : FriendLedgerEntry {
        override val timestampMillis get() = settlement.date.toEpochDay() * 86_400_000L
    }
}

data class FriendDetailUiState(
    val friend: Friend? = null,
    val netMinor: Long = 0L,
    val ledger: List<FriendLedgerEntry> = emptyList(),
    val reminderSent: Boolean = false,
    val reminderError: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val deleteFriendUseCase: DeleteFriendUseCase,
    private val reminderRepository: ReminderRepository,
    balanceRepository: BalanceRepository,
    settlementRepository: SettlementRepository,
    sharedExpenseRepository: SharedExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: String = requireNotNull(savedStateHandle["friendId"])
    private val friend = MutableStateFlow<Friend?>(null)
    private val reminderSent = MutableStateFlow(false)
    private val reminderError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch { friend.value = friendRepository.getFriend(friendId) }
    }

    val uiState: StateFlow<FriendDetailUiState> = combine(
        friend,
        balanceRepository.observeOverallBalance(friendId),
        settlementRepository.observeForFriend(friendId),
        sharedExpenseRepository.observeForFriend(friendId),
        reminderSent,
        reminderError
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val f = values[0] as Friend?
        @Suppress("UNCHECKED_CAST")
        val net = values[1] as Long
        @Suppress("UNCHECKED_CAST")
        val settlements = values[2] as List<Settlement>
        @Suppress("UNCHECKED_CAST")
        val friendExpenses = values[3] as List<SharedExpense>
        val sent = values[4] as Boolean
        val error = values[5] as String?

        val ledger = buildList<FriendLedgerEntry> {
            settlements.forEach { add(FriendLedgerEntry.SettlementEntry(it)) }
            friendExpenses.forEach { add(FriendLedgerEntry.ExpenseEntry(it)) }
        }.sortedByDescending { it.timestampMillis }

        FriendDetailUiState(friend = f, netMinor = net, ledger = ledger, reminderSent = sent, reminderError = error, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FriendDetailUiState())

    fun deleteFriend(onDone: () -> Unit) {
        viewModelScope.launch {
            deleteFriendUseCase(friendId)
            onDone()
        }
    }

    fun sendSettlementReminder() {
        val f = friend.value ?: return
        val linkedUserId = f.linkedUserId
        if (linkedUserId == null) {
            reminderError.value = "${f.name} hasn't joined Expense Tracker yet"
            return
        }
        viewModelScope.launch {
            val net = uiState.value.netMinor
            reminderRepository.sendSettlementReminder(linkedUserId, f.name, net, null).fold(
                onSuccess = { reminderSent.value = true },
                onFailure = { reminderError.value = it.message ?: "Couldn't send reminder" }
            )
        }
    }

    fun clearReminderState() {
        reminderSent.value = false
        reminderError.value = null
    }
}
