package com.omer.expensetracker.data.repository.split

import com.omer.expensetracker.data.local.dao.split.ActivityLogDao
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.domain.model.split.ActivityLogEntry
import com.omer.expensetracker.domain.repository.split.ActivityLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActivityLogRepositoryImpl @Inject constructor(
    private val activityLogDao: ActivityLogDao
) : ActivityLogRepository {

    override fun observeAll(): Flow<List<ActivityLogEntry>> =
        activityLogDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeForGroup(groupId: String): Flow<List<ActivityLogEntry>> =
        activityLogDao.observeForGroup(groupId).map { list -> list.map { it.toDomain() } }
}
