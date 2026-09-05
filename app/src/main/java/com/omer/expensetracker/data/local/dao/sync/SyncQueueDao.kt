package com.omer.expensetracker.data.local.dao.sync

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omer.expensetracker.data.local.entity.sync.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(entry: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun pending(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun observePendingCount(): Flow<Int>

    @Delete
    suspend fun remove(entry: SyncQueueEntity)

    @Query("DELETE FROM sync_queue")
    suspend fun clear()
}
