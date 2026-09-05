package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.model.split.FriendGroup
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.GroupRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

class AddGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(name: String, iconKey: String, colorArgb: Long, memberFriendIds: List<String>): UseCaseResult<FriendGroup> {
        if (name.isBlank()) return UseCaseResult.Failure("Enter a group name")
        val members = (memberFriendIds + YOU_FRIEND_ID).distinct()
        return UseCaseResult.Success(groupRepository.addGroup(name.trim(), iconKey, colorArgb, members))
    }
}

class UpdateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(group: FriendGroup): UseCaseResult<Unit> {
        if (group.name.isBlank()) return UseCaseResult.Failure("Enter a group name")
        groupRepository.updateGroup(group)
        return UseCaseResult.Success(Unit)
    }
}

class ArchiveGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(id: String, archived: Boolean) = groupRepository.setArchived(id, archived)
}

class AddGroupMemberUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String, friendId: String) = groupRepository.addMember(groupId, friendId)
}

class RemoveGroupMemberUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String, friendId: String) = groupRepository.removeMember(groupId, friendId)
}
