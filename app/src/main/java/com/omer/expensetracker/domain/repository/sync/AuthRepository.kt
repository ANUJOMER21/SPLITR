package com.omer.expensetracker.domain.repository.sync

import com.omer.expensetracker.domain.model.sync.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<AuthUser?>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signOut()
}
