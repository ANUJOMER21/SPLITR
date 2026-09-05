package com.omer.expensetracker.domain.model

/** Special category id that every install seeds and that entries fall back to when their
 * original category is deleted. Never hidden, never deletable. */
const val OTHER_CATEGORY_ID = "category_other"

/** Default category every savings-goal contribution is booked against, so money moved into a
 * goal still shows up as spending on that day. Seeded on every install. */
const val SAVINGS_CATEGORY_ID = "category_savings"

data class Category(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long,
    val isDefault: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
