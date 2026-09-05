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
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    linkedSharedExpenseId = linkedSharedExpenseId
)

fun Entry.toEntity(): EntryEntity = EntryEntity(
    id = id,
    type = type.name,
    amountMinor = amountMinor,
    categoryId = categoryId,
    dateEpochDay = date.toEpochDay(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    linkedSharedExpenseId = linkedSharedExpenseId
)
