package com.omer.expensetracker.data.local.dao

import androidx.room.Embedded
import com.omer.expensetracker.data.local.entity.CategoryEntity

data class CategoryTotalRow(
    @Embedded val category: CategoryEntity,
    val totalMinor: Long
)
