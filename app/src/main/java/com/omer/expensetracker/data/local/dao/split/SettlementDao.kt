package com.omer.expensetracker.data.local.dao.split

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.split.SettlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    @Query("SELECT * FROM settlements WHERE isDeleted = 0 ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements WHERE isDeleted = 0 AND (payerFriendId = :friendId OR receiverFriendId = :friendId) ORDER BY dateEpochDay DESC")
    fun observeForFriend(friendId: String): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements WHERE id = :id")
    suspend fun getById(id: String): SettlementEntity?

    @Insert
    suspend fun insert(settlement: SettlementEntity)

    @Update
    suspend fun update(settlement: SettlementEntity)

    @Query("UPDATE settlements SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)
}
