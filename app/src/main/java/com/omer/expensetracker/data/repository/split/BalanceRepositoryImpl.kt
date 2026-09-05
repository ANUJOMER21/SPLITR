package com.omer.expensetracker.data.repository.split

import com.omer.expensetracker.data.local.dao.split.BalanceDao
import com.omer.expensetracker.domain.model.split.Balance
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BalanceRepositoryImpl @Inject constructor(
    private val balanceDao: BalanceDao
) : BalanceRepository {

    override fun observeOverallBalance(friendId: String): Flow<Long> =
        balanceDao.observeOverallBalance(friendId).map { it?.netMinor ?: 0L }

    override fun observeFriendBalances(friendId: String): Flow<List<Balance>> =
        balanceDao.observeForFriend(friendId).map { list -> list.map { Balance(it.friendId, it.groupId, it.netMinor) } }

    override fun observeGroupBalances(groupId: String): Flow<List<Balance>> =
        balanceDao.observeForGroup(groupId).map { list -> list.map { Balance(it.friendId, it.groupId, it.netMinor) } }

    override fun observeAllOverallBalances(): Flow<List<Balance>> =
        balanceDao.observeAllOverall().map { list -> list.map { Balance(it.friendId, it.groupId, it.netMinor) } }
}
