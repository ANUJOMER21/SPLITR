package com.omer.expensetracker.data.repository.split

import com.omer.expensetracker.data.local.dao.split.FriendDao
import com.omer.expensetracker.data.local.entity.split.FriendEntity
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class FriendRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao,
    private val syncOutbox: SyncOutbox
) : FriendRepository {

    override fun observeFriends(): Flow<List<Friend>> =
        friendDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getFriend(id: String): Friend? = friendDao.getById(id)?.toDomain()

    override suspend fun getYou(): Friend = friendDao.getYou()?.toDomain()
        ?: requireNotNull(friendDao.getById(YOU_FRIEND_ID)?.toDomain()) { "You record missing" }

    override suspend fun getFriendByLinkedUserId(uid: String): Friend? = friendDao.getByLinkedUserId(uid)?.toDomain()

    override suspend fun addFriend(name: String, email: String, contactInfo: String?, avatarColorArgb: Long, linkedUserId: String?): Friend {
        val now = System.currentTimeMillis()
        val friend = Friend(
            id = UUID.randomUUID().toString(), name = name, email = email, contactInfo = contactInfo,
            avatarColorArgb = avatarColorArgb, isYou = false, linkedUserId = linkedUserId, createdAt = now, updatedAt = now
        )
        friendDao.insert(
            FriendEntity(
                id = friend.id, name = friend.name, email = friend.email, contactInfo = friend.contactInfo,
                avatarColorArgb = friend.avatarColorArgb, isYou = false, linkedUserId = linkedUserId, createdAt = now, updatedAt = now
            )
        )
        syncOutbox.enqueue(SyncEntityType.FRIEND, friend.id, SyncOperation.UPSERT)
        return friend
    }

    override suspend fun updateFriend(friend: Friend) {
        friendDao.update(
            FriendEntity(
                id = friend.id, name = friend.name, email = friend.email, contactInfo = friend.contactInfo,
                avatarColorArgb = friend.avatarColorArgb, isYou = friend.isYou, linkedUserId = friend.linkedUserId,
                createdAt = friend.createdAt, updatedAt = System.currentTimeMillis()
            )
        )
        syncOutbox.enqueue(SyncEntityType.FRIEND, friend.id, SyncOperation.UPSERT)
    }

    override suspend fun deleteFriend(id: String) {
        friendDao.softDelete(id, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.FRIEND, id, SyncOperation.DELETE)
    }

    override suspend fun linkYouIdentity(email: String, uid: String) {
        friendDao.updateYouIdentity(email, uid, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.FRIEND, YOU_FRIEND_ID, SyncOperation.UPSERT)
    }

    override suspend fun upsertFromRemote(id: String, name: String, email: String, avatarColorArgb: Long, linkedUserId: String?) {
        val now = System.currentTimeMillis()
        val existing = friendDao.getById(id)
        val entity = FriendEntity(
            id = id, name = name, email = email, contactInfo = existing?.contactInfo,
            avatarColorArgb = avatarColorArgb, isYou = false, linkedUserId = linkedUserId,
            createdAt = existing?.createdAt ?: now, updatedAt = now
        )
        if (existing == null) friendDao.insert(entity) else friendDao.update(entity)
    }
}
