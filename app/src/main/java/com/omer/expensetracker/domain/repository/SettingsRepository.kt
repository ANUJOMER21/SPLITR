package com.omer.expensetracker.domain.repository

import kotlinx.coroutines.flow.Flow

/** Lightweight per-device app preferences, backed by DataStore. Not synced. */
interface SettingsRepository {
    /** False until the user finishes (or skips) the first-run onboarding. */
    val onboardingComplete: Flow<Boolean>
    /** `null` means the flag has never been written — i.e. this is a genuinely fresh install,
     * not an upgrade over existing data. */
    val onboardingCompleteRaw: Flow<Boolean?>
    suspend fun setOnboardingComplete(value: Boolean)

    /** When true, the app requires a biometric / device-credential unlock on each cold start
     * and each return from the background. */
    val appLockEnabled: Flow<Boolean>
    suspend fun setAppLockEnabled(value: Boolean)
}
