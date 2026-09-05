package com.omer.expensetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.omer.expensetracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val onboardingComplete: Flow<Boolean> =
        dataStore.data.map { it[KEY_ONBOARDING_COMPLETE] ?: false }

    override val onboardingCompleteRaw: Flow<Boolean?> =
        dataStore.data.map { it[KEY_ONBOARDING_COMPLETE] }

    override suspend fun setOnboardingComplete(value: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = value }
    }

    override val appLockEnabled: Flow<Boolean> =
        dataStore.data.map { it[KEY_APP_LOCK_ENABLED] ?: false }

    override suspend fun setAppLockEnabled(value: Boolean) {
        dataStore.edit { it[KEY_APP_LOCK_ENABLED] = value }
    }

    private companion object {
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    }
}
