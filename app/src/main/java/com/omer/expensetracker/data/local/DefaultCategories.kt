package com.omer.expensetracker.data.local

import com.omer.expensetracker.domain.model.OTHER_CATEGORY_ID
import com.omer.expensetracker.domain.model.SAVINGS_CATEGORY_ID

/** Icon key -> presentation layer maps these to actual Material icons ([CategoryIconProvider]). */
data class DefaultCategorySeed(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long
)

val DEFAULT_CATEGORY_SEEDS = listOf(
    DefaultCategorySeed("category_food", "Food", "food", 0xFFFF7043),
    DefaultCategorySeed("category_transport", "Transport", "transport", 0xFF42A5F5),
    DefaultCategorySeed("category_bills", "Bills & Utilities", "bills", 0xFFAB47BC),
    DefaultCategorySeed("category_shopping", "Shopping", "shopping", 0xFFEC407A),
    DefaultCategorySeed("category_entertainment", "Entertainment", "entertainment", 0xFF7E57C2),
    DefaultCategorySeed("category_health", "Health", "health", 0xFF26A69A),
    DefaultCategorySeed("category_groceries", "Groceries", "groceries", 0xFF66BB6A),
    DefaultCategorySeed(SAVINGS_CATEGORY_ID, "Savings", "savings", 0xFF26C6DA),
    DefaultCategorySeed(OTHER_CATEGORY_ID, "Other", "other", 0xFF78909C)
)
