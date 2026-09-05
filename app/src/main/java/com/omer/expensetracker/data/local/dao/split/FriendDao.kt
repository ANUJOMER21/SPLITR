package com.omer.expensetracker.data.local.dao.split

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.split.FriendEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends WHERE isDeleted = 0 ORDER BY isYou DESC, name ASC")
    fun observeAll(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun getById(id: String): FriendEntity?

    @Query("SELECT * FROM friends WHERE isYou = 1 LIMIT 1")
    suspend fun getYou(): FriendEntity?

    @Query("SELECT * FROM friends WHERE linkedUserId = :uid LIMIT 1")
    suspend fun getByLinkedUserId(uid: String): FriendEntity?

    // REPLACE, not the default ABORT: concurrent Firestore listeners (groups/expenses/
    // settlements/friends all resolving the same newly-seen uid at once) can race an
    // insert-vs-insert for the same friend id — REPLACE makes that last-write-wins instead
    // of crashing with a UNIQUE constraint violation.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(friend: FriendEntity)

    @Update
    suspend fun update(friend: FriendEntity)

    @Query("UPDATE friends SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE friends SET email = :email, linkedUserId = :linkedUserId, updatedAt = :updatedAt WHERE isYou = 1")
    suspend fun updateYouIdentity(email: String, linkedUserId: String, updatedAt: Long)
}
