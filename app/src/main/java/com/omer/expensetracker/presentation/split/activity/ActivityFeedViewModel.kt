package com.omer.expensetracker.presentation.split.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.split.ActivityLogEntry
import com.omer.expensetracker.domain.repository.split.ActivityLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ActivityFeedUiState(val entries: List<ActivityLogEntry> = emptyList(), val isLoading: Boolean = true)

@HiltViewModel
class ActivityFeedViewModel @Inject constructor(
    activityLogRepository: ActivityLogRepository
) : ViewModel() {

    val uiState: StateFlow<ActivityFeedUiState> = activityLogRepository.observeAll()
        .map { ActivityFeedUiState(entries = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActivityFeedUiState())
}
