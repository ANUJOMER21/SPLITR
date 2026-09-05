package com.omer.expensetracker.data.mapper

import com.omer.expensetracker.data.local.entity.RecurringRuleEntity
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.RecurrenceUnit
import com.omer.expensetracker.domain.model.RecurringRule
import java.time.LocalDate

fun RecurringRuleEntity.toDomain(): RecurringRule = RecurringRule(
    id = id,
    type = EntryType.valueOf(type),
    amountMinor = amountMinor,
    categoryId = categoryId,
    unit = RecurrenceUnit.valueOf(unit),
    intervalCount = intervalCount,
    startDate = LocalDate.ofEpochDay(startDateEpochDay),
    endDate = endDateEpochDay?.let { LocalDate.ofEpochDay(it) },
    lastGeneratedDate = lastGeneratedDateEpochDay?.let { LocalDate.ofEpochDay(it) },
    nextDueDate = LocalDate.ofEpochDay(nextDueDateEpochDay),
    isPaused = isPaused,
    skipNextOccurrence = skipNextOccurrence,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RecurringRule.toEntity(): RecurringRuleEntity = RecurringRuleEntity(
    id = id,
    type = type.name,
    amountMinor = amountMinor,
    categoryId = categoryId,
    unit = unit.name,
    intervalCount = intervalCount,
    startDateEpochDay = startDate.toEpochDay(),
    endDateEpochDay = endDate?.toEpochDay(),
    lastGeneratedDateEpochDay = lastGeneratedDate?.toEpochDay(),
    nextDueDateEpochDay = nextDueDate.toEpochDay(),
    isPaused = isPaused,
    skipNextOccurrence = skipNextOccurrence,
    createdAt = createdAt,
    updatedAt = updatedAt
)
