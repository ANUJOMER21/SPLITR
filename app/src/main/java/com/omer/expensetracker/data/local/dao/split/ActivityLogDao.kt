package com.omer.expensetracker.data.local.dao.split

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.omer.expensetracker.data.local.entity.split.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_log WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun observeForGroup(groupId: String): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insert(entry: ActivityLogEntity)
}
