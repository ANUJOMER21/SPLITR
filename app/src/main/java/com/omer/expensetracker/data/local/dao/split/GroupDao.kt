package com.omer.expensetracker.data.local.dao.split

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.split.GroupEntity
import com.omer.expensetracker.data.local.entity.split.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM friend_groups WHERE isDeleted = 0 AND isArchived = 0 ORDER BY name ASC")
    fun observeActive(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM friend_groups WHERE isDeleted = 0 ORDER BY isArchived ASC, name ASC")
    fun observeAll(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM friend_groups WHERE id = :id")
    suspend fun getById(id: String): GroupEntity?

    @Insert
    suspend fun insert(group: GroupEntity)

    @Update
    suspend fun update(group: GroupEntity)

    @Query("UPDATE friend_groups SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE friend_groups SET isArchived = :archived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, updatedAt: Long)

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun observeMembers(groupId: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT friendId FROM group_members WHERE groupId = :groupId")
    suspend fun getMemberIds(groupId: String): List<String>

    @Insert
    suspend fun addMember(member: GroupMemberEntity)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND friendId = :friendId")
    suspend fun removeMember(groupId: String, friendId: String)
}
