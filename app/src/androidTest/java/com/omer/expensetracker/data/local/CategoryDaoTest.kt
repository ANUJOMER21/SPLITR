package com.omer.expensetracker.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.local.entity.CategoryEntity
import com.omer.expensetracker.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var entryDao: EntryDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        categoryDao = db.categoryDao()
        entryDao = db.entryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun category(id: String, name: String, isActive: Boolean = true) = CategoryEntity(
        id = id,
        name = name,
        iconKey = "other",
        colorArgb = 0xFF000000,
        isDefault = false,
        isActive = isActive,
        createdAt = 0L,
        updatedAt = 0L
    )

    @Test
    fun observeActiveCategories_excludesHiddenOnes() = runTest {
        categoryDao.insert(category("cat_visible", "Visible"))
        categoryDao.insert(category("cat_hidden", "Hidden", isActive = false))

        val active = categoryDao.observeActiveCategories().first()

        assertEquals(1, active.size)
        assertEquals("cat_visible", active.first().id)
    }

    @Test
    fun setActive_togglesVisibilityWithoutDeleting() = runTest {
        categoryDao.insert(category("cat1", "Test"))

        categoryDao.setActive("cat1", isActive = false, updatedAt = 1L)

        val stored = categoryDao.getById("cat1")
        assertFalse(stored!!.isActive)
    }

    @Test
    fun categoriesWithMonthTotal_sumsOnlyCurrentMonthExpenses() = runTest {
        categoryDao.insert(category("cat_food", "Food"))
        val today = LocalDate.of(2026, 9, 4)
        entryDao.insert(
            EntryEntity("e1", "EXPENSE", 1500L, "cat_food", today.toEpochDay(), createdAt = 0L, updatedAt = 0L)
        )
        entryDao.insert(
            EntryEntity("e2", "EXPENSE", 9999L, "cat_food", today.minusMonths(1).toEpochDay(), createdAt = 0L, updatedAt = 0L)
        )

        val start = today.withDayOfMonth(1).toEpochDay()
        val end = today.withDayOfMonth(today.lengthOfMonth()).toEpochDay()
        val rows = categoryDao.observeCategoriesWithMonthTotal(start, end).first()

        assertEquals(1500L, rows.first { it.category.id == "cat_food" }.totalMinor)
    }

    @Test
    fun categoriesWithMonthTotal_includesCategoriesWithNoEntries() = runTest {
        categoryDao.insert(category("cat_empty", "Empty"))

        val rows = categoryDao.observeCategoriesWithMonthTotal(0L, 100000L).first()

        assertTrue(rows.any { it.category.id == "cat_empty" && it.totalMinor == 0L })
    }
}
