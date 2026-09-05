package com.omer.expensetracker.data.mapper

import com.omer.expensetracker.data.local.entity.BillReminderEntity
import com.omer.expensetracker.domain.model.BillReminder
import com.omer.expensetracker.domain.model.RecurrenceUnit
import java.time.LocalDate

fun BillReminderEntity.toDomain(): BillReminder = BillReminder(
    id = id,
    label = label,
    estimatedAmountMinor = estimatedAmountMinor,
    categoryId = categoryId,
    dueDate = LocalDate.ofEpochDay(dueDateEpochDay),
    leadDays = leadDays,
    repeatUnit = repeatUnit?.let { RecurrenceUnit.valueOf(it) },
    repeatIntervalCount = repeatIntervalCount,
    isPaid = isPaid,
    paidEntryId = paidEntryId,
    lastNotifiedForDueDate = lastNotifiedForDueDateEpochDay?.let { LocalDate.ofEpochDay(it) },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BillReminder.toEntity(): BillReminderEntity = BillReminderEntity(
    id = id,
    label = label,
    estimatedAmountMinor = estimatedAmountMinor,
    categoryId = categoryId,
    dueDateEpochDay = dueDate.toEpochDay(),
    leadDays = leadDays,
    repeatUnit = repeatUnit?.name,
    repeatIntervalCount = repeatIntervalCount,
    isPaid = isPaid,
    paidEntryId = paidEntryId,
    lastNotifiedForDueDateEpochDay = lastNotifiedForDueDate?.toEpochDay(),
    createdAt = createdAt,
    updatedAt = updatedAt
)
