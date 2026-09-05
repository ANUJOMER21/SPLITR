package com.omer.expensetracker.presentation.split.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.SplitType
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.split.GroupRepository
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import com.omer.expensetracker.domain.usecase.split.AddSharedExpenseUseCase
import com.omer.expensetracker.domain.usecase.split.DeleteSharedExpenseUseCase
import com.omer.expensetracker.domain.usecase.split.EditSharedExpenseUseCase
import com.omer.expensetracker.domain.usecase.split.SplitInput
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.presentation.navigation.NEW_ID
import com.omer.expensetracker.presentation.util.parseAmountToMinorUnits
import com.omer.expensetracker.presentation.util.toAmountInputString
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SplitMode { EQUAL, EXACT, PERCENTAGE, SHARES }

data class AddSharedExpenseUiState(
    val isEditing: Boolean = false,
    val description: String = "",
    val amountText: String = "",
    val date: LocalDate = LocalDate.now(),
    val availableParticipants: List<Friend> = emptyList(),
    val selectedParticipantIds: Set<String> = setOf(YOU_FRIEND_ID),
    val selectedPayerIds: Set<String> = setOf(YOU_FRIEND_ID),
    val payerAmountText: Map<String, String> = emptyMap(),
    val splitMode: SplitMode = SplitMode.EQUAL,
    val exactAmountText: Map<String, String> = emptyMap(),
    val percentageText: Map<String, String> = emptyMap(),
    val sharesText: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class AddSharedExpenseViewModel @Inject constructor(
    friendRepository: FriendRepository,
    groupRepository: GroupRepository,
    private val sharedExpenseRepository: SharedExpenseRepository,
    private val addSharedExpenseUseCase: AddSharedExpenseUseCase,
    private val editSharedExpenseUseCase: EditSharedExpenseUseCase,
    private val deleteSharedExpenseUseCase: DeleteSharedExpenseUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = requireNotNull(savedStateHandle["groupId"])
    private val expenseId: String? = savedStateHandle.get<String>("expenseId")?.takeIf { it != NEW_ID }
    private val isEditing = expenseId != null

    private val description = MutableStateFlow("")
    private val amountText = MutableStateFlow("")
    private val date = MutableStateFlow(LocalDate.now())
    private val selectedParticipantIds = MutableStateFlow(setOf(YOU_FRIEND_ID))
    private val selectedPayerIds = MutableStateFlow(setOf(YOU_FRIEND_ID))
    private val payerAmountText = MutableStateFlow<Map<String, String>>(emptyMap())
    private val splitMode = MutableStateFlow(SplitMode.EQUAL)
    private val exactAmountText = MutableStateFlow<Map<String, String>>(emptyMap())
    private val percentageText = MutableStateFlow<Map<String, String>>(emptyMap())
    private val sharesText = MutableStateFlow<Map<String, String>>(emptyMap())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val isSaved = MutableStateFlow(false)
    private val isDeleted = MutableStateFlow(false)

    private val availableParticipants = kotlinx.coroutines.flow.combine(
        groupRepository.observeMemberIds(groupId), friendRepository.observeFriends()
    ) { memberIds, friends -> friends.filter { it.id in memberIds || it.isYou } }

    init {
        val id = expenseId
        if (id != null) {
            viewModelScope.launch {
                val detail = sharedExpenseRepository.getDetail(id) ?: return@launch
                description.value = detail.expense.description
                amountText.value = detail.expense.amountMinor.toAmountInputString()
                date.value = detail.expense.date
                selectedPayerIds.value = detail.paidByFriendId.keys
                payerAmountText.value = detail.paidByFriendId.mapValues { it.value.toAmountInputString() }
                selectedParticipantIds.value = detail.owedByFriendId.keys
                val total = detail.expense.amountMinor.coerceAtLeast(1L)
                splitMode.value = when (detail.expense.splitType) {
                    SplitType.EQUAL -> SplitMode.EQUAL
                    SplitType.EXACT -> SplitMode.EXACT
                    SplitType.PERCENTAGE -> SplitMode.PERCENTAGE
                    SplitType.SHARES -> SplitMode.SHARES
                    SplitType.ITEMIZED -> SplitMode.EXACT
                }
                exactAmountText.value = detail.owedByFriendId.mapValues { it.value.toAmountInputString() }
                // Percentages aren't persisted separately from the resulting owed amounts, so
                // re-derive them for display — re-saving without changes reproduces the same
                // split to within a rounding cent via the largest-remainder distribution.
                percentageText.value = detail.owedByFriendId.mapValues {
                    "%.2f".format(it.value * 100.0 / total)
                }
            }
        }
    }

    val uiState: StateFlow<AddSharedExpenseUiState> = combine(
        description, amountText, date, availableParticipants, selectedParticipantIds,
        selectedPayerIds, payerAmountText, splitMode, exactAmountText, percentageText, sharesText,
        errorMessage
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AddSharedExpenseUiState(
            isEditing = isEditing,
            description = values[0] as String,
            amountText = values[1] as String,
            date = values[2] as LocalDate,
            availableParticipants = values[3] as List<Friend>,
            selectedParticipantIds = values[4] as Set<String>,
            selectedPayerIds = values[5] as Set<String>,
            payerAmountText = values[6] as Map<String, String>,
            splitMode = values[7] as SplitMode,
            exactAmountText = values[8] as Map<String, String>,
            percentageText = values[9] as Map<String, String>,
            sharesText = values[10] as Map<String, String>,
            errorMessage = values[11] as String?
        )
    }.combine(isSaved) { state, saved -> state.copy(isSaved = saved) }
        .combine(isDeleted) { state, deleted -> state.copy(isDeleted = deleted) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddSharedExpenseUiState(isEditing = isEditing))

    fun onDescriptionChange(value: String) { description.value = value; errorMessage.value = null }
    fun onAmountChange(value: String) { amountText.value = value; errorMessage.value = null }
    fun onDateChange(value: LocalDate) { date.value = value }
    fun onSplitModeSelect(mode: SplitMode) { splitMode.value = mode }

    fun togglePayer(friendId: String) {
        val current = selectedPayerIds.value
        selectedPayerIds.value = if (friendId in current) {
            (current - friendId).ifEmpty { current }
        } else {
            current + friendId
        }
    }

    fun onPayerAmountChange(friendId: String, value: String) {
        payerAmountText.value = payerAmountText.value + (friendId to value)
    }

    fun toggleParticipant(friendId: String) {
        selectedParticipantIds.value = if (friendId in selectedParticipantIds.value) {
            selectedParticipantIds.value - friendId
        } else {
            selectedParticipantIds.value + friendId
        }
    }
    fun onExactAmountChange(friendId: String, value: String) { exactAmountText.value = exactAmountText.value + (friendId to value) }
    fun onPercentageChange(friendId: String, value: String) { percentageText.value = percentageText.value + (friendId to value) }
    fun onSharesChange(friendId: String, value: String) { sharesText.value = sharesText.value + (friendId to value) }

    fun save() {
        val amountMinor = amountText.value.parseAmountToMinorUnits()
        if (amountMinor == null || amountMinor <= 0L) {
            errorMessage.value = "Enter a valid amount"
            return
        }
        val participants = selectedParticipantIds.value.toList()
        if (participants.isEmpty()) {
            errorMessage.value = "Select at least one participant"
            return
        }
        val payers = selectedPayerIds.value.toList()
        if (payers.isEmpty()) {
            errorMessage.value = "Select at least one payer"
            return
        }
        val paidByFriendId = if (payers.size == 1) {
            mapOf(payers.first() to amountMinor)
        } else {
            payers.associateWith { (payerAmountText.value[it]?.parseAmountToMinorUnits()) ?: 0L }
        }
        if (paidByFriendId.values.sum() != amountMinor) {
            errorMessage.value = "Payer amounts must add up to the total"
            return
        }
        val split = when (splitMode.value) {
            SplitMode.EQUAL -> SplitInput.Equal(participants)
            SplitMode.EXACT -> SplitInput.Exact(
                participants.associateWith { (exactAmountText.value[it]?.parseAmountToMinorUnits()) ?: 0L }
            )
            SplitMode.PERCENTAGE -> SplitInput.Percentage(
                participants.associateWith { (percentageText.value[it]?.toDoubleOrNull()) ?: 0.0 }
            )
            SplitMode.SHARES -> SplitInput.Shares(
                participants.associateWith { (sharesText.value[it]?.toIntOrNull()) ?: 1 }
            )
        }

        viewModelScope.launch {
            val id = expenseId
            val result: UseCaseResult<*> = if (id != null) {
                editSharedExpenseUseCase(
                    id = id,
                    description = description.value,
                    amountMinor = amountMinor,
                    categoryId = null,
                    date = date.value,
                    groupId = groupId,
                    photoUri = null,
                    paidByFriendId = paidByFriendId,
                    split = split
                )
            } else {
                addSharedExpenseUseCase(
                    description = description.value,
                    amountMinor = amountMinor,
                    categoryId = null,
                    date = date.value,
                    groupId = groupId,
                    photoUri = null,
                    paidByFriendId = paidByFriendId,
                    split = split
                )
            }
            when (result) {
                is UseCaseResult.Success -> isSaved.value = true
                is UseCaseResult.Failure -> errorMessage.value = result.message
            }
        }
    }

    fun delete() {
        val id = expenseId ?: return
        viewModelScope.launch {
            deleteSharedExpenseUseCase(id)
            isDeleted.value = true
        }
    }
}
