package com.omer.expensetracker.presentation.split.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.split.GroupRepository
import com.omer.expensetracker.domain.usecase.split.RecordSettlementUseCase
import com.omer.expensetracker.domain.usecase.split.SettlementAllocator
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

/** One place a settlement can land: a shared group, or the non-group ("direct") bucket when
 * [groupId] is null. [outstandingMinor] is the positive amount this bucket can still absorb in
 * the currently chosen direction. */
data class SettleBucket(
    val groupId: String?,
    val label: String,
    val outstandingMinor: Long,
    val selected: Boolean
)

data class SettleUpUiState(
    val friend: Friend? = null,
    val amountText: String = "",
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    /** true = friend is paying You (they owe you); false = You are paying the friend. */
    val friendIsPaying: Boolean = true,
    /** Per-group / direct breakdown of what's outstanding. One entry => plain settlement;
     * more than one => the user picks which buckets this payment clears. */
    val buckets: List<SettleBucket> = emptyList(),
    val errorMessage: String? = null,
    val isSaved: Boolean = false
) {
    val isMultiBucket: Boolean get() = buckets.size > 1
    val selectedOutstandingMinor: Long get() = buckets.filter { it.selected }.sumOf { it.outstandingMinor }
}

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val recordSettlementUseCase: RecordSettlementUseCase,
    balanceRepository: BalanceRepository,
    groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: String = requireNotNull(savedStateHandle["friendId"])
    /** Set when opened from a group's Balances tab — locks the flow to that one group. */
    private val lockedGroupId: String? = savedStateHandle.get<String>("groupId")?.takeIf { it.isNotBlank() }

    private val friend = MutableStateFlow<Friend?>(null)
    private val amountText = MutableStateFlow("")
    private val amountTouched = MutableStateFlow(false)
    private val date = MutableStateFlow(LocalDate.now())
    private val note = MutableStateFlow("")
    private val friendIsPaying = MutableStateFlow(true)
    private val deselectedGroupIds = MutableStateFlow<Set<String?>>(emptySet())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val isSaved = MutableStateFlow(false)

    /** Rebuilt whenever balances, group names, the chosen direction, or the user's bucket
     * selection change. */
    private val buckets: StateFlow<List<SettleBucket>> = combine(
        balanceRepository.observeFriendBalances(friendId),
        groupRepository.observeAllGroups(),
        friendIsPaying,
        deselectedGroupIds
    ) { balances, groups, friendPaying, deselected ->
        val nameByGroupId = groups.associate { it.id to it.name }
        fun label(groupId: String?) = groupId?.let { nameByGroupId[it] ?: "Group" } ?: "Non-group expenses"

        val overall = balances.firstOrNull { it.groupId == null }?.netMinor ?: 0L
        val groupNet = balances.filter { it.groupId != null }
        val directNet = overall - groupNet.sumOf { it.netMinor }

        if (lockedGroupId != null) {
            val net = balances.firstOrNull { it.groupId == lockedGroupId }?.netMinor ?: 0L
            return@combine listOf(SettleBucket(lockedGroupId, label(lockedGroupId), kotlin.math.abs(net), selected = true))
        }

        val signed = buildList {
            groupNet.filter { it.netMinor != 0L }.forEach { add(it.groupId to it.netMinor) }
            if (directNet != 0L) add(null to directNet)
        }
        signed
            .filter { (_, net) -> if (friendPaying) net > 0L else net < 0L }
            .map { (groupId, net) ->
                SettleBucket(groupId, label(groupId), kotlin.math.abs(net), selected = groupId !in deselected)
            }
            .sortedByDescending { it.outstandingMinor }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { friend.value = friendRepository.getFriend(friendId) }
        // Keep the amount field mirroring the selected buckets until the user edits it.
        viewModelScope.launch {
            buckets.collect { list ->
                if (!amountTouched.value) {
                    val sum = list.filter { it.selected }.sumOf { it.outstandingMinor }
                    amountText.value = if (sum == 0L) "" else formatMinorPlain(sum)
                }
            }
        }
        // Initial direction guess from the friend's overall balance.
        viewModelScope.launch {
            balanceRepository.observeOverallBalance(friendId).collect { current ->
                if (!amountTouched.value && current != 0L) friendIsPaying.value = current > 0L
            }
        }
    }

    val uiState: StateFlow<SettleUpUiState> = combine(
        friend, amountText, date, note, friendIsPaying, errorMessage, isSaved, buckets
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SettleUpUiState(
            friend = values[0] as Friend?,
            amountText = values[1] as String,
            date = values[2] as LocalDate,
            note = values[3] as String,
            friendIsPaying = values[4] as Boolean,
            errorMessage = values[5] as String?,
            isSaved = values[6] as Boolean,
            buckets = values[7] as List<SettleBucket>
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettleUpUiState())

    fun onAmountChange(value: String) { amountText.value = value; amountTouched.value = true; errorMessage.value = null }
    fun onDateChange(value: LocalDate) { date.value = value }
    fun onNoteChange(value: String) { note.value = value }
    fun onDirectionSelect(friendIsPayingValue: Boolean) {
        friendIsPaying.value = friendIsPayingValue
        amountTouched.value = false
        deselectedGroupIds.value = emptySet()
    }

    fun onToggleBucket(groupId: String?) {
        deselectedGroupIds.value = deselectedGroupIds.value.toMutableSet().apply {
            if (contains(groupId)) remove(groupId) else add(groupId)
        }
        amountTouched.value = false
    }

    fun save() {
        val amountMinor = amountText.value.parseAmountToMinorUnits()
        if (amountMinor == null || amountMinor <= 0L) {
            errorMessage.value = "Enter a valid amount"
            return
        }
        val payer = if (friendIsPaying.value) friendId else YOU_FRIEND_ID
        val receiver = if (friendIsPaying.value) YOU_FRIEND_ID else friendId
        val currentBuckets = buckets.value

        viewModelScope.launch {
            if (currentBuckets.size > 1) {
                val selected = currentBuckets.filter { it.selected }
                if (selected.isEmpty()) { errorMessage.value = "Pick at least one group to settle"; return@launch }
                val allocation = SettlementAllocator.allocate(
                    selected.associate { it.groupId to it.outstandingMinor },
                    amountMinor
                )
                if (allocation.isEmpty()) { errorMessage.value = "Nothing outstanding to settle"; return@launch }
                when (val result = recordSettlementUseCase.allocated(payer, receiver, date.value, note.value.ifBlank { null }, allocation)) {
                    is UseCaseResult.Success -> isSaved.value = true
                    is UseCaseResult.Failure -> errorMessage.value = result.message
                }
            } else {
                val groupId = currentBuckets.firstOrNull()?.groupId ?: lockedGroupId
                when (val result = recordSettlementUseCase(payer, receiver, amountMinor, date.value, note.value.ifBlank { null }, groupId)) {
                    is UseCaseResult.Success -> isSaved.value = true
                    is UseCaseResult.Failure -> errorMessage.value = result.message
                }
            }
        }
    }
}

private fun formatMinorPlain(amountMinor: Long): String =
    (amountMinor / 100.0).let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
