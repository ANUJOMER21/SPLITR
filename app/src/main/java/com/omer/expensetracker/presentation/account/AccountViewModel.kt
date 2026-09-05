package com.omer.expensetracker.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.sync.SyncState
import com.omer.expensetracker.domain.repository.sync.AuthRepository
import com.omer.expensetracker.domain.repository.sync.CloudSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cloudSyncRepository: CloudSyncRepository
) : ViewModel() {

    val syncState: StateFlow<SyncState> = cloudSyncRepository.observeSyncState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncState())

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
