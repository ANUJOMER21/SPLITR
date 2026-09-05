package com.omer.expensetracker.presentation.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.data.debug.SplitSampleDataSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val splitSampleDataSeeder: SplitSampleDataSeeder
) : ViewModel() {

    private val _seedResult = MutableStateFlow<String?>(null)
    val seedResult: StateFlow<String?> = _seedResult
    private val _seeding = MutableStateFlow(false)
    val seeding: StateFlow<Boolean> = _seeding

    fun seedSampleSplitData() {
        if (_seeding.value) return
        _seeding.value = true
        viewModelScope.launch {
            _seedResult.value = runCatching { splitSampleDataSeeder.seed() }
                .getOrElse { "Seed failed: ${it.message}" }
            _seeding.value = false
        }
    }

    fun consumeSeedResult() { _seedResult.value = null }
}
