package com.omer.expensetracker.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.data.repository.AppDataResetter
import com.omer.expensetracker.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDataResetter: AppDataResetter,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    private val _resetting = MutableStateFlow(false)
    val resetting: StateFlow<Boolean> = _resetting

    val appLockEnabled: StateFlow<Boolean> = settingsRepository.appLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setAppLockEnabled(value: Boolean) {
        viewModelScope.launch { settingsRepository.setAppLockEnabled(value) }
    }

    fun resetAllData() {
        if (_resetting.value) return
        _resetting.value = true
        viewModelScope.launch {
            _message.value = runCatching { appDataResetter.reset(); "All local data cleared." }
                .getOrElse { "Reset failed: ${it.message}" }
            _resetting.value = false
        }
    }

    fun replayOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete(false)
            onDone()
        }
    }

    fun consumeMessage() { _message.value = null }
}
