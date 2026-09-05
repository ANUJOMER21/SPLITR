package com.omer.expensetracker.data.repository.sync

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.domain.model.sync.AuthUser
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.sync.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Sign-In only — no password to store or leak. Every method checks
 * [FeatureFlags.CLOUD_SYNC_ENABLED] before touching [auth]/[firestore]; there is no default
 * [com.google.firebase.FirebaseApp] usable without a real `google-services.json`, so calling
 * into the Firebase SDK outside the flag guard would throw.
 */
@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val friendRepository: FriendRepository
) : AuthRepository {

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val firestore by lazy { Firebase.firestore }

    override fun observeCurrentUser(): Flow<AuthUser?> {
        if (!FeatureFlags.CLOUD_SYNC_ENABLED) return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { fa ->
                trySend(fa.currentUser?.let { AuthUser(it.uid, it.email, it.displayName) })
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        if (!FeatureFlags.CLOUD_SYNC_ENABLED) return Result.failure(IllegalStateException("Cloud sync is disabled"))
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(credential).await().user
                ?: error("Google sign-in returned no user")
            onSignedIn(user)
            AuthUser(user.uid, user.email, user.displayName)
        }
    }

    override suspend fun signOut() {
        if (!FeatureFlags.CLOUD_SYNC_ENABLED) return
        auth.signOut()
    }

    /** Runs once per sign-in: mirrors the profile into the private `users/{uid}` doc and the
     * public, email-searchable `userDirectory/{uid}` doc, stamps this device's identity onto
     * the local "You" friend row so shared expenses resolve correctly, and registers this
     * device's FCM token so other people's reminders/activity pushes can reach it. */
    private suspend fun onSignedIn(user: FirebaseUser) {
        val email = user.email ?: return
        firestore.collection("users").document(user.uid).set(
            mapOf(
                "email" to email, "displayName" to user.displayName, "photoUrl" to user.photoUrl?.toString(),
                "lastSignedInAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
        firestore.collection("userDirectory").document(user.uid).set(
            mapOf("email" to email, "displayName" to user.displayName)
        ).await()
        friendRepository.linkYouIdentity(email, user.uid)
        runCatching {
            val token = Firebase.messaging.token.await()
            firestore.collection("users").document(user.uid)
                .collection("fcmTokens").document(token).set(mapOf("updatedAt" to System.currentTimeMillis())).await()
        }
    }
}
