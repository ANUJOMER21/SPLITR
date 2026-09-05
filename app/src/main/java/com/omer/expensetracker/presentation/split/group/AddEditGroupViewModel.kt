package com.omer.expensetracker.presentation.split.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.usecase.split.AddGroupUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.presentation.categories.CATEGORY_COLOR_PALETTE
import com.omer.expensetracker.presentation.util.CategoryIconProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditGroupUiState(
    val name: String = "",
    val iconKey: String = CategoryIconProvider.pickableKeys.first(),
    val colorArgb: Long = CATEGORY_COLOR_PALETTE.random(),
    val allFriends: List<Friend> = emptyList(),
    val selectedFriendIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditGroupViewModel @Inject constructor(
    friendRepository: FriendRepository,
    private val addGroupUseCase: AddGroupUseCase
) : ViewModel() {

    private val name = MutableStateFlow("")
    private val iconKey = MutableStateFlow(CategoryIconProvider.pickableKeys.first())
    private val colorArgb = MutableStateFlow(CATEGORY_COLOR_PALETTE.random())
    private val selectedFriendIds = MutableStateFlow<Set<String>>(emptySet())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val isSaved = MutableStateFlow(false)

    val uiState: StateFlow<AddEditGroupUiState> = combine(
        name, iconKey, colorArgb, friendRepository.observeFriends(), selectedFriendIds, errorMessage, isSaved
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AddEditGroupUiState(
            name = values[0] as String,
            iconKey = values[1] as String,
            colorArgb = values[2] as Long,
            allFriends = (values[3] as List<Friend>).filter { !it.isYou },
            selectedFriendIds = values[4] as Set<String>,
            errorMessage = values[5] as String?,
            isSaved = values[6] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditGroupUiState())

    fun onNameChange(value: String) { name.value = value; errorMessage.value = null }
    fun onIconSelect(key: String) { iconKey.value = key }
    fun onColorSelect(color: Long) { colorArgb.value = color }
    fun toggleFriend(friendId: String) {
        selectedFriendIds.value = if (friendId in selectedFriendIds.value) selectedFriendIds.value - friendId else selectedFriendIds.value + friendId
    }

    fun save() {
        viewModelScope.launch {
            when (val result = addGroupUseCase(name.value, iconKey.value, colorArgb.value, selectedFriendIds.value.toList())) {
                is UseCaseResult.Success -> isSaved.value = true
                is UseCaseResult.Failure -> errorMessage.value = result.message
            }
        }
    }
}
