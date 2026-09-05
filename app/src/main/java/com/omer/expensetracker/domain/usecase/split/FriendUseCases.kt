package com.omer.expensetracker.domain.usecase.split

import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.sync.UserDirectoryRepository
import com.omer.expensetracker.domain.util.UseCaseResult
import javax.inject.Inject

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

data class AddFriendOutcome(val friend: Friend, val isLinked: Boolean)

class AddFriendUseCase @Inject constructor(
    private val friendRepository: FriendRepository,
    private val userDirectoryRepository: UserDirectoryRepository
) {
    suspend operator fun invoke(name: String, email: String, contactInfo: String?, avatarColorArgb: Long): UseCaseResult<AddFriendOutcome> {
        if (name.isBlank()) return UseCaseResult.Failure("Enter a name")
        val trimmedEmail = email.trim()
        if (!EMAIL_REGEX.matches(trimmedEmail)) return UseCaseResult.Failure("Enter a valid email — it's how we sync with them")

        val linked = userDirectoryRepository.findByEmail(trimmedEmail)
        val friend = friendRepository.addFriend(name.trim(), trimmedEmail, contactInfo?.trim()?.ifBlank { null }, avatarColorArgb, linked?.uid)
        return UseCaseResult.Success(AddFriendOutcome(friend, isLinked = linked != null))
    }
}

class UpdateFriendUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(friend: Friend): UseCaseResult<Unit> {
        if (friend.name.isBlank()) return UseCaseResult.Failure("Enter a name")
        friendRepository.updateFriend(friend)
        return UseCaseResult.Success(Unit)
    }
}

class DeleteFriendUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {
    suspend operator fun invoke(id: String) = friendRepository.deleteFriend(id)
}
