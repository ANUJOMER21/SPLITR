package com.omer.expensetracker.data.debug

import com.omer.expensetracker.domain.model.split.SplitType
import com.omer.expensetracker.domain.model.split.YOU_FRIEND_ID
import com.omer.expensetracker.domain.repository.split.ExpenseWrite
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.split.GroupRepository
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import com.omer.expensetracker.domain.usecase.split.SplitCalculator
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * Dev-only: populates the Split-with-friends feature with a realistic set of friends, two
 * overlapping groups, and a spread of shared expenses (some you paid, some a friend paid) plus
 * a couple of non-group expenses — enough to exercise the multi-group settle-up flow. Idempotent:
 * a second run is a no-op once the sample friends already exist. Not wired into release builds
 * (see FeatureFlags.SPLIT_SAMPLE_DATA_SEED).
 */
class SplitSampleDataSeeder @Inject constructor(
    private val friendRepository: FriendRepository,
    private val groupRepository: GroupRepository,
    private val sharedExpenseRepository: SharedExpenseRepository
) {

    private data class SampleFriend(val name: String, val email: String, val colorArgb: Long)

    private val sampleFriends = listOf(
        SampleFriend("Alice", "alice.sample@example.com", 0xFFEF4444L),
        SampleFriend("Bob", "bob.sample@example.com", 0xFF3B82F6L),
        SampleFriend("Carol", "carol.sample@example.com", 0xFF10B981L),
        SampleFriend("Dave", "dave.sample@example.com", 0xFFF59E0BL)
    )

    /** @return a short human-readable summary of what was created, or why it was skipped. */
    suspend fun seed(): String {
        val existing = friendRepository.observeFriends().first()
        if (existing.any { it.email in sampleFriends.map(SampleFriend::email) }) {
            return "Sample split data already present — nothing added."
        }

        val idByName = HashMap<String, String>()
        sampleFriends.forEach { s ->
            val friend = friendRepository.addFriend(s.name, s.email, null, s.colorArgb, null)
            idByName[s.name] = friend.id
        }
        fun fid(name: String) = idByName.getValue(name)

        val goa = groupRepository.addGroup(
            name = "Goa Trip", iconKey = "shopping-cart", colorArgb = 0xFF6366F1L,
            memberFriendIds = listOf(fid("Alice"), fid("Bob"), fid("Carol"), YOU_FRIEND_ID)
        )
        val flat = groupRepository.addGroup(
            name = "Flat 402", iconKey = "shopping-cart", colorArgb = 0xFF14B8A6L,
            memberFriendIds = listOf(fid("Bob"), fid("Carol"), fid("Dave"), YOU_FRIEND_ID)
        )

        val today = LocalDate.now()
        var count = 0

        suspend fun expense(
            description: String, amountMinor: Long, groupId: String?,
            payer: String, participants: List<String>, daysAgo: Long
        ) {
            val owed = SplitCalculator.equalSplit(amountMinor, participants)
            sharedExpenseRepository.addExpense(
                ExpenseWrite(
                    description = description, amountMinor = amountMinor, categoryId = null,
                    splitType = SplitType.EQUAL, date = today.minusDays(daysAgo), groupId = groupId,
                    photoUri = null,
                    paidByFriendId = mapOf(payer to amountMinor),
                    owedByFriendId = owed
                )
            )
            count++
        }

        val goaMembers = listOf(YOU_FRIEND_ID, fid("Alice"), fid("Bob"), fid("Carol"))
        expense("Hotel", 12_000_00, goa.id, YOU_FRIEND_ID, goaMembers, 12)
        expense("Beach dinner", 3_200_00, goa.id, fid("Alice"), goaMembers, 10)
        expense("Airport cab", 1_600_00, goa.id, fid("Bob"), goaMembers, 9)

        val flatMembers = listOf(YOU_FRIEND_ID, fid("Bob"), fid("Carol"), fid("Dave"))
        expense("Groceries", 4_000_00, flat.id, YOU_FRIEND_ID, flatMembers, 8)
        expense("Internet bill", 1_200_00, flat.id, fid("Carol"), flatMembers, 6)
        expense("Electricity", 2_400_00, flat.id, YOU_FRIEND_ID, flatMembers, 3)

        expense("Concert tickets", 5_000_00, null, YOU_FRIEND_ID, listOf(YOU_FRIEND_ID, fid("Alice")), 5)
        expense("Weekday lunch", 900_00, null, fid("Bob"), listOf(YOU_FRIEND_ID, fid("Bob")), 2)

        return "Added ${sampleFriends.size} friends, 2 groups, $count shared expenses."
    }
}
