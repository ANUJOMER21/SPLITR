package com.omer.expensetracker.data.repository.split

import com.omer.expensetracker.data.local.dao.split.GroupDao
import com.omer.expensetracker.data.local.entity.split.GroupEntity
import com.omer.expensetracker.data.local.entity.split.GroupMemberEntity
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
import com.omer.expensetracker.domain.model.split.FriendGroup
import com.omer.expensetracker.domain.repository.split.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val syncOutbox: SyncOutbox
) : GroupRepository {

    override fun observeActiveGroups(): Flow<List<FriendGroup>> =
        groupDao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeAllGroups(): Flow<List<FriendGroup>> =
        groupDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getGroup(id: String): FriendGroup? = groupDao.getById(id)?.toDomain()

    override fun observeMemberIds(groupId: String): Flow<List<String>> =
        groupDao.observeMembers(groupId).map { list -> list.map { it.friendId } }

    override suspend fun addGroup(name: String, iconKey: String, colorArgb: Long, memberFriendIds: List<String>): FriendGroup {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        groupDao.insert(
            GroupEntity(id = id, name = name, iconKey = iconKey, colorArgb = colorArgb, isArchived = false, createdAt = now, updatedAt = now)
        )
        memberFriendIds.distinct().forEach { friendId ->
            groupDao.addMember(GroupMemberEntity(groupId = id, friendId = friendId, joinedAt = now))
        }
        syncOutbox.enqueue(SyncEntityType.GROUP, id, SyncOperation.UPSERT)
        return FriendGroup(id, name, iconKey, colorArgb, isArchived = false, createdAt = now, updatedAt = now)
    }

    override suspend fun updateGroup(group: FriendGroup) {
        groupDao.update(
            GroupEntity(
                id = group.id, name = group.name, iconKey = group.iconKey, colorArgb = group.colorArgb,
                isArchived = group.isArchived, createdAt = group.createdAt, updatedAt = System.currentTimeMillis()
            )
        )
        syncOutbox.enqueue(SyncEntityType.GROUP, group.id, SyncOperation.UPSERT)
    }

    override suspend fun setArchived(id: String, archived: Boolean) {
        groupDao.setArchived(id, archived, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.GROUP, id, SyncOperation.UPSERT)
    }

    override suspend fun addMember(groupId: String, friendId: String) {
        groupDao.addMember(GroupMemberEntity(groupId, friendId, System.currentTimeMillis()))
        syncOutbox.enqueue(SyncEntityType.GROUP, groupId, SyncOperation.UPSERT)
    }

    override suspend fun removeMember(groupId: String, friendId: String) {
        groupDao.removeMember(groupId, friendId)
        syncOutbox.enqueue(SyncEntityType.GROUP, groupId, SyncOperation.UPSERT)
    }

    override suspend fun upsertFromRemote(id: String, name: String, iconKey: String, colorArgb: Long, memberFriendIds: List<String>, isArchived: Boolean) {
        val now = System.currentTimeMillis()
        val existing = groupDao.getById(id)
        val entity = GroupEntity(
            id = id, name = name, iconKey = iconKey, colorArgb = colorArgb, isArchived = isArchived,
            createdAt = existing?.createdAt ?: now, updatedAt = now
        )
        if (existing == null) groupDao.insert(entity) else groupDao.update(entity)

        val currentMemberIds = groupDao.getMemberIds(id).toSet()
        val targetMemberIds = memberFriendIds.distinct().toSet()
        (targetMemberIds - currentMemberIds).forEach { groupDao.addMember(GroupMemberEntity(id, it, now)) }
        (currentMemberIds - targetMemberIds).forEach { groupDao.removeMember(id, it) }
    }
}
