package com.omer.expensetracker.domain.repository.sync

data class LinkedUserRef(val uid: String, val email: String, val displayName: String?)

/** Looks people up by email in the public user directory — how adding a friend resolves to a
 * real linked account (see [com.omer.expensetracker.domain.model.split.Friend.linkedUserId]). */
interface UserDirectoryRepository {
    suspend fun findByEmail(email: String): LinkedUserRef?
}
