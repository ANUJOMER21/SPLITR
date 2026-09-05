package com.omer.expensetracker.data.mapper

import com.omer.expensetracker.data.local.dao.BudgetProgressRow
import com.omer.expensetracker.data.local.entity.BudgetEntity
import com.omer.expensetracker.domain.model.Budget
import com.omer.expensetracker.domain.model.BudgetProgress

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    categoryId = categoryId,
    monthlyLimitMinor = monthlyLimitMinor,
    warningNotifiedMonth = warningNotifiedMonth,
    breachNotifiedMonth = breachNotifiedMonth,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BudgetProgressRow.toDomain(): BudgetProgress = BudgetProgress(
    budget = budget.toDomain(),
    category = category?.toDomain(),
    spentMinor = spentMinor,
    limitMinor = budget.monthlyLimitMinor
)
