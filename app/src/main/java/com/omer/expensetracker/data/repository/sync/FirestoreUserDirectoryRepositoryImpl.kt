package com.omer.expensetracker.data.repository.sync

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.omer.expensetracker.FeatureFlags
import com.omer.expensetracker.domain.repository.sync.LinkedUserRef
import com.omer.expensetracker.domain.repository.sync.UserDirectoryRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserDirectoryRepositoryImpl @Inject constructor() : UserDirectoryRepository {

    private val firestore by lazy { Firebase.firestore }

    override suspend fun findByEmail(email: String): LinkedUserRef? {
        if (!FeatureFlags.CLOUD_SYNC_ENABLED) return null
        return runCatching {
            val snapshot = firestore.collection("userDirectory")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull() ?: return null
            LinkedUserRef(uid = doc.id, email = email, displayName = doc.getString("displayName"))
        }.getOrNull()
    }
}
