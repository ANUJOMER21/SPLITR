package com.omer.expensetracker.data.local.dao.split

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.omer.expensetracker.data.local.entity.split.BalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {
    @Query("SELECT * FROM balances WHERE id = :id")
    suspend fun getById(id: String): BalanceEntity?

    @Upsert
    suspend fun upsert(balance: BalanceEntity)

    @Query("SELECT * FROM balances WHERE friendId = :friendId AND groupId IS NULL")
    fun observeOverallBalance(friendId: String): Flow<BalanceEntity?>

    @Query("SELECT * FROM balances WHERE friendId = :friendId")
    fun observeForFriend(friendId: String): Flow<List<BalanceEntity>>

    @Query("SELECT * FROM balances WHERE groupId = :groupId")
    fun observeForGroup(groupId: String): Flow<List<BalanceEntity>>

    @Query("SELECT * FROM balances WHERE groupId IS NULL")
    fun observeAllOverall(): Flow<List<BalanceEntity>>
}
