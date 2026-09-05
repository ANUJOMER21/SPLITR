package com.omer.expensetracker.presentation.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.usecase.category.AddCategoryUseCase
import com.omer.expensetracker.domain.usecase.category.GetCategoryUseCase
import com.omer.expensetracker.domain.usecase.category.UpdateCategoryUseCase
import com.omer.expensetracker.domain.util.UseCaseResult
import com.omer.expensetracker.presentation.navigation.NEW_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

val CATEGORY_COLOR_PALETTE = listOf(
    0xFFFF7043, 0xFF42A5F5, 0xFFAB47BC, 0xFFEC407A,
    0xFF7E57C2, 0xFF26A69A, 0xFF66BB6A, 0xFF78909C,
    0xFFFFA726, 0xFF29B6F6, 0xFF8D6E63, 0xFFEF5350
)

data class AddEditCategoryUiState(
    val isEditing: Boolean = false,
    val isDefault: Boolean = false,
    val name: String = "",
    val iconKey: String = "other",
    val colorArgb: Long = CATEGORY_COLOR_PALETTE.first(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

private const val KEY_NAME = "name"
private const val KEY_ICON = "iconKey"
private const val KEY_COLOR = "colorArgb"
private const val KEY_INITIALIZED = "initialized"

@HiltViewModel
class AddEditCategoryViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = savedStateHandle["categoryId"] ?: NEW_ID
    private val isEditing = categoryId != NEW_ID

    private val name = savedStateHandle.getStateFlow(KEY_NAME, "")
    private val iconKey = savedStateHandle.getStateFlow(KEY_ICON, "other")
    private val colorArgb = savedStateHandle.getStateFlow(KEY_COLOR, CATEGORY_COLOR_PALETTE.first())
    private val errorFlow = MutableStateFlow<String?>(null)
    private val savedFlow = MutableStateFlow(false)
    private val loadingFlow = MutableStateFlow(isEditing)
    private var isDefault = false

    private val formFlow = combine(name, iconKey, colorArgb) { n, icon, color -> Triple(n, icon, color) }

    init {
        if (savedStateHandle.get<Boolean>(KEY_INITIALIZED) != true) {
            savedStateHandle[KEY_INITIALIZED] = true
            if (isEditing) {
                viewModelScope.launch {
                    val category = getCategoryUseCase(categoryId)
                    if (category != null) {
                        isDefault = category.isDefault
                        savedStateHandle[KEY_NAME] = category.name
                        savedStateHandle[KEY_ICON] = category.iconKey
                        savedStateHandle[KEY_COLOR] = category.colorArgb
                    }
                    loadingFlow.value = false
                }
            }
        } else {
            loadingFlow.value = false
        }
    }

    val uiState: StateFlow<AddEditCategoryUiState> = combine(
        formFlow, errorFlow, savedFlow, loadingFlow
    ) { form, error, saved, loading ->
        AddEditCategoryUiState(
            isEditing = isEditing,
            isDefault = isDefault,
            name = form.first,
            iconKey = form.second,
            colorArgb = form.third,
            errorMessage = error,
            isLoading = loading,
            isSaved = saved
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditCategoryUiState(isEditing = isEditing))

    fun onNameChange(value: String) {
        savedStateHandle[KEY_NAME] = value
        errorFlow.value = null
    }

    fun onIconSelect(key: String) {
        savedStateHandle[KEY_ICON] = key
    }

    fun onColorSelect(color: Long) {
        savedStateHandle[KEY_COLOR] = color
    }

    fun save() {
        viewModelScope.launch {
            val result: UseCaseResult<Unit> = if (isEditing) {
                val existing = getCategoryUseCase(categoryId)
                if (existing == null) {
                    UseCaseResult.Failure("Category not found")
                } else {
                    updateCategoryUseCase(
                        existing.copy(name = name.value, iconKey = iconKey.value, colorArgb = colorArgb.value)
                    )
                }
            } else {
                addCategoryUseCase(name.value, iconKey.value, colorArgb.value).let {
                    when (it) {
                        is UseCaseResult.Success -> UseCaseResult.Success(Unit)
                        is UseCaseResult.Failure -> it
                    }
                }
            }
            when (result) {
                is UseCaseResult.Success -> savedFlow.value = true
                is UseCaseResult.Failure -> errorFlow.value = result.message
            }
        }
    }
}
