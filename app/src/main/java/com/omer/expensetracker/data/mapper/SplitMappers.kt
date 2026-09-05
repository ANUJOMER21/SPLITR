package com.omer.expensetracker.data.mapper

import com.omer.expensetracker.data.local.entity.split.ActivityLogEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseCommentEntity
import com.omer.expensetracker.data.local.entity.split.FriendEntity
import com.omer.expensetracker.data.local.entity.split.GroupEntity
import com.omer.expensetracker.data.local.entity.split.SettlementEntity
import com.omer.expensetracker.data.local.entity.split.SharedExpenseEntity
import com.omer.expensetracker.domain.model.split.ActivityLogEntry
import com.omer.expensetracker.domain.model.split.ActivityType
import com.omer.expensetracker.domain.model.split.ExpenseComment
import com.omer.expensetracker.domain.model.split.Friend
import com.omer.expensetracker.domain.model.split.FriendGroup
import com.omer.expensetracker.domain.model.split.SharedExpense
import com.omer.expensetracker.domain.model.split.Settlement
import com.omer.expensetracker.domain.model.split.SplitType
import java.time.LocalDate

fun FriendEntity.toDomain() = Friend(
    id = id, name = name, email = email, contactInfo = contactInfo, avatarColorArgb = avatarColorArgb,
    isYou = isYou, linkedUserId = linkedUserId, createdAt = createdAt, updatedAt = updatedAt
)

fun GroupEntity.toDomain() = FriendGroup(
    id = id, name = name, iconKey = iconKey, colorArgb = colorArgb, isArchived = isArchived,
    createdAt = createdAt, updatedAt = updatedAt
)

fun SharedExpenseEntity.toDomain() = SharedExpense(
    id = id, description = description, amountMinor = amountMinor, categoryId = categoryId,
    splitType = SplitType.valueOf(splitType), date = LocalDate.ofEpochDay(dateEpochDay),
    groupId = groupId, photoUri = photoUri, createdAt = createdAt, updatedAt = updatedAt,
    isDeleted = isDeleted
)

fun SettlementEntity.toDomain() = Settlement(
    id = id, payerFriendId = payerFriendId, receiverFriendId = receiverFriendId,
    amountMinor = amountMinor, date = LocalDate.ofEpochDay(dateEpochDay), note = note,
    groupId = groupId, batchId = batchId, createdAt = createdAt, updatedAt = updatedAt, isDeleted = isDeleted
)

fun ActivityLogEntity.toDomain() = ActivityLogEntry(
    id = id, groupId = groupId, type = ActivityType.valueOf(type), summary = summary, timestamp = timestamp
)

fun ExpenseCommentEntity.toDomain() = ExpenseComment(id = id, expenseId = expenseId, text = text, createdAt = createdAt)
