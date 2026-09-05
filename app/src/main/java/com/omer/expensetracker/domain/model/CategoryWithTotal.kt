package com.omer.expensetracker.domain.model

data class CategoryWithTotal(
    val category: Category,
    val currentMonthTotalMinor: Long
)
