package com.omer.expensetracker.data.mapper

import com.omer.expensetracker.data.local.entity.EntryEntity
import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryType
import java.time.LocalDate

fun EntryEntity.toDomain(): Entry = Entry(
    id = id,
    type = EntryType.valueOf(type),
    amountMinor = amountMinor,
    categoryId = categoryId,
    date = LocalDate.ofEpochDay(dateEpochDay),
    note = note,
    photoUri = photoUri,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    linkedSharedExpenseId = linkedSharedExpenseId,
    linkedGoalContributionId = linkedGoalContributionId
)

fun Entry.toEntity(): EntryEntity = EntryEntity(
    id = id,
    type = type.name,
    amountMinor = amountMinor,
    categoryId = categoryId,
    dateEpochDay = date.toEpochDay(),
    note = note?.trim()?.ifBlank { null },
    photoUri = photoUri?.ifBlank { null },
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    linkedSharedExpenseId = linkedSharedExpenseId,
    linkedGoalContributionId = linkedGoalContributionId
)
