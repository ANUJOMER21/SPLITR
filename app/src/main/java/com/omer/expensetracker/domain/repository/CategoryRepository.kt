package com.omer.expensetracker.domain.repository

import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.CategoryWithTotal
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeActiveCategories(): Flow<List<Category>>
    fun observeAllCategories(): Flow<List<Category>>
    fun observeCategoriesWithCurrentMonthTotal(): Flow<List<CategoryWithTotal>>
    suspend fun getCategory(id: String): Category?
    suspend fun addCategory(name: String, iconKey: String, colorArgb: Long): Category
    suspend fun updateCategory(category: Category)
    suspend fun setCategoryActive(id: String, isActive: Boolean)

    /** Reassigns every entry referencing [id] to [OTHER_CATEGORY_ID], then deactivates it. */
    suspend fun deleteCustomCategory(id: String)

    /** Cloud-sync-only: merge a category pulled from this account's Firestore backup. */
    suspend fun upsertFromRemote(category: Category)
}
