package com.omer.expensetracker.data.local.dao

import androidx.room.Embedded
import com.omer.expensetracker.data.local.entity.BudgetEntity
import com.omer.expensetracker.data.local.entity.CategoryEntity

data class BudgetProgressRow(
    @Embedded val budget: BudgetEntity,
    @Embedded(prefix = "cat_") val category: CategoryEntity?,
    val spentMinor: Long
)
