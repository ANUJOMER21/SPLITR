package com.omer.expensetracker.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory "is the app currently unlocked" gate. Process-scoped: a cold start begins locked
 * (when app lock is enabled), and [lock] is called whenever the app is backgrounded so the
 * next foreground requires re-auth.
 */
@Singleton
class AppLockManager @Inject constructor() {
    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked

    fun unlock() { _unlocked.value = true }
    fun lock() { _unlocked.value = false }
}
