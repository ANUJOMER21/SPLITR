package com.omer.expensetracker.presentation.split.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.usecase.split.AddFriendUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.presentation.categories.CATEGORY_COLOR_PALETTE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditFriendUiState(
    val name: String = "",
    val email: String = "",
    val contactInfo: String = "",
    val colorArgb: Long = CATEGORY_COLOR_PALETTE.random(),
    val errorMessage: String? = null,
    val notRegisteredNotice: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditFriendViewModel @Inject constructor(
    private val addFriendUseCase: AddFriendUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditFriendUiState())
    val uiState: StateFlow<AddEditFriendUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null, notRegisteredNotice = null)
    }

    fun onContactInfoChange(value: String) {
        _uiState.value = _uiState.value.copy(contactInfo = value)
    }

    fun onColorSelect(colorArgb: Long) {
        _uiState.value = _uiState.value.copy(colorArgb = colorArgb)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            when (val result = addFriendUseCase(state.name, state.email, state.contactInfo.ifBlank { null }, state.colorArgb)) {
                is UseCaseResult.Success -> {
                    _uiState.value = if (result.data.isLinked) {
                        state.copy(isSaved = true)
                    } else {
                        // Still saved locally — balance tracking works right away — but flag
                        // that they won't see anything sync to their own device until they join.
                        state.copy(isSaved = true, notRegisteredNotice = "${state.name.trim()} isn't on Expense Tracker yet")
                    }
                }
                is UseCaseResult.Failure -> _uiState.value = state.copy(errorMessage = result.message)
            }
        }
    }
}
