package com.omer.expensetracker.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.domain.repository.sync.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Whether the app requires sign-in at all, and — once that's known — whether the user
 * currently satisfies it. `null` means "still checking", so the splash screen and the nav
 * graph's sign-out guard can both wait for a real answer instead of flashing the wrong screen. */
@HiltViewModel
class AuthGateViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val isSignedIn: StateFlow<Boolean?> = if (FeatureFlags.CLOUD_SYNC_ENABLED) {
        authRepository.observeCurrentUser().map { it != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        kotlinx.coroutines.flow.MutableStateFlow(true)
    }
}
