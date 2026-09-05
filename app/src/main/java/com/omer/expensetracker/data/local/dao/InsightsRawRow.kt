package com.omer.expensetracker.data.local.dao

data class InsightsRawRow(
    val id: String,
    val type: String,
    val amountMinor: Long,
    val categoryId: String?,
    val dateEpochDay: Long
)
