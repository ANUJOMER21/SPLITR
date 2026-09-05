package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.data.mapper.toEntity
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.CategoryWithTotal
import com.omer.expensetracker.domain.model.OTHER_CATEGORY_ID
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.data.repository.sync.SyncEntityType
import com.omer.expensetracker.data.repository.sync.SyncOperation
import com.omer.expensetracker.data.repository.sync.SyncOutbox
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val entryDao: EntryDao,
    private val syncOutbox: SyncOutbox
) : CategoryRepository {

    override fun observeActiveCategories(): Flow<List<Category>> =
        categoryDao.observeActiveCategories().map { list -> list.map { it.toDomain() } }

    override fun observeAllCategories(): Flow<List<Category>> =
        categoryDao.observeAllCategories().map { list -> list.map { it.toDomain() } }

    override fun observeCategoriesWithCurrentMonthTotal(): Flow<List<CategoryWithTotal>> {
        val month = YearMonth.now()
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return categoryDao.observeCategoriesWithMonthTotal(start, end).map { rows ->
            rows.map { row -> CategoryWithTotal(row.category.toDomain(), row.totalMinor) }
        }
    }

    override suspend fun getCategory(id: String): Category? = categoryDao.getById(id)?.toDomain()

    override suspend fun addCategory(name: String, iconKey: String, colorArgb: Long): Category {
        val now = System.currentTimeMillis()
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            iconKey = iconKey,
            colorArgb = colorArgb,
            isDefault = false,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
        categoryDao.insert(category.toEntity())
        syncOutbox.enqueue(SyncEntityType.CATEGORY, category.id, SyncOperation.UPSERT)
        return category
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.update(category.toEntity())
        syncOutbox.enqueue(SyncEntityType.CATEGORY, category.id, SyncOperation.UPSERT)
    }

    override suspend fun setCategoryActive(id: String, isActive: Boolean) {
        categoryDao.setActive(id, isActive, System.currentTimeMillis())
        syncOutbox.enqueue(SyncEntityType.CATEGORY, id, SyncOperation.UPSERT)
    }

    override suspend fun deleteCustomCategory(id: String) {
        val now = System.currentTimeMillis()
        entryDao.reassignCategory(categoryId = id, fallbackCategoryId = OTHER_CATEGORY_ID, updatedAt = now)
        categoryDao.setActive(id, isActive = false, updatedAt = now)
        syncOutbox.enqueue(SyncEntityType.CATEGORY, id, SyncOperation.UPSERT)
    }

    override suspend fun upsertFromRemote(category: Category) {
        val existing = categoryDao.getById(category.id)
        // Last-write-wins: ignore an older remote copy.
        if (existing != null && existing.updatedAt >= category.updatedAt) return
        categoryDao.upsert(category.toEntity())
    }
}
