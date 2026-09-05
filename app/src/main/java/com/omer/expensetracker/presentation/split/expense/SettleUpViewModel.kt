package com.omer.expensetracker.presentation.split.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.usecase.split.RecordSettlementUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.presentation.util.parseAmountToMinorUnits
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettleUpUiState(
    val friend: Friend? = null,
    val amountText: String = "",
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    /** true = friend is paying You (they owe you); false = You are paying the friend. */
    val friendIsPaying: Boolean = true,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val recordSettlementUseCase: RecordSettlementUseCase,
    balanceRepository: BalanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: String = requireNotNull(savedStateHandle["friendId"])
    private val groupId: String? = savedStateHandle.get<String>("groupId")?.takeIf { it.isNotBlank() }

    private val friend = MutableStateFlow<Friend?>(null)
    private val amountText = MutableStateFlow("")
    private val date = MutableStateFlow(LocalDate.now())
    private val note = MutableStateFlow("")
    private val friendIsPaying = MutableStateFlow(true)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val isSaved = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            friend.value = friendRepository.getFriend(friendId)
            val net = balanceRepository.observeOverallBalance(friendId)
            net.collect { current ->
                if (amountText.value.isEmpty()) {
                    amountText.value = if (current == 0L) "" else (kotlin.math.abs(current) / 100.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
                    friendIsPaying.value = current >= 0
                }
            }
        }
    }

    val uiState: StateFlow<SettleUpUiState> = combine(
        friend, amountText, date, note, friendIsPaying, errorMessage, isSaved
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SettleUpUiState(
            friend = values[0] as Friend?,
            amountText = values[1] as String,
            date = values[2] as LocalDate,
            note = values[3] as String,
            friendIsPaying = values[4] as Boolean,
            errorMessage = values[5] as String?,
            isSaved = values[6] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettleUpUiState())

    fun onAmountChange(value: String) { amountText.value = value; errorMessage.value = null }
    fun onDateChange(value: LocalDate) { date.value = value }
    fun onNoteChange(value: String) { note.value = value }
    fun onDirectionSelect(friendIsPayingValue: Boolean) { friendIsPaying.value = friendIsPayingValue }

    fun save() {
        val amountMinor = amountText.value.parseAmountToMinorUnits()
        if (amountMinor == null || amountMinor <= 0L) {
            errorMessage.value = "Enter a valid amount"
            return
        }
        val payer = if (friendIsPaying.value) friendId else YOU_FRIEND_ID
        val receiver = if (friendIsPaying.value) YOU_FRIEND_ID else friendId
        viewModelScope.launch {
            when (val result = recordSettlementUseCase(payer, receiver, amountMinor, date.value, note.value.ifBlank { null }, groupId)) {
                is UseCaseResult.Success -> isSaved.value = true
                is UseCaseResult.Failure -> errorMessage.value = result.message
            }
        }
    }
}
