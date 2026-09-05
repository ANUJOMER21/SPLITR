package com.omer.expensetracker.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.local.entity.CategoryEntity
import com.omer.expensetracker.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class EntryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var entryDao: EntryDao

    private val today = LocalDate.of(2026, 9, 4)

    @Before
    fun setUp() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        entryDao = db.entryDao()

        db.categoryDao().insert(category("cat_food", "Food"))
        db.categoryDao().insert(category("cat_transport", "Transport"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun category(id: String, name: String) = CategoryEntity(
        id = id,
        name = name,
        iconKey = "other",
        colorArgb = 0xFF000000,
        isDefault = false,
        isActive = true,
        createdAt = 0L,
        updatedAt = 0L
    )

    private fun entry(
        id: String,
        type: String,
        amountMinor: Long,
        categoryId: String?,
        date: LocalDate,
        isDeleted: Boolean = false
    ) = EntryEntity(
        id = id,
        type = type,
        amountMinor = amountMinor,
        categoryId = categoryId,
        dateEpochDay = date.toEpochDay(),
        createdAt = 0L,
        updatedAt = 0L,
        isDeleted = isDeleted
    )

    @Test
    fun monthlyTotals_sumExpenseAndIncomeSeparately() = runTest {
        entryDao.insert(entry("e1", "EXPENSE", 1000L, "cat_food", today))
        entryDao.insert(entry("e2", "EXPENSE", 500L, "cat_transport", today))
        entryDao.insert(entry("e3", "INCOME", 5000L, null, today))
        // outside the month — must not be counted
        entryDao.insert(entry("e4", "EXPENSE", 9999L, "cat_food", today.minusMonths(1)))

        val start = today.withDayOfMonth(1).toEpochDay()
        val end = today.withDayOfMonth(today.lengthOfMonth()).toEpochDay()
        val totals = entryDao.observeMonthlyTotals(start, end).first()

        assertEquals(1500L, totals.first { it.type == "EXPENSE" }.total)
        assertEquals(5000L, totals.first { it.type == "INCOME" }.total)
    }

    @Test
    fun softDeletedEntries_areExcludedFromFilteredQuery() = runTest {
        entryDao.insert(entry("e1", "EXPENSE", 1000L, "cat_food", today))
        entryDao.softDelete("e1", updatedAt = 1L)

        val results = entryDao.observeFiltered(
            com.omer.expensetracker.data.repository.EntryFilterQueryBuilder.build(
                com.omer.expensetracker.domain.model.EntryFilter()
            )
        ).first()

        assertTrue(results.isEmpty())
    }

    @Test
    fun restore_bringsBackASoftDeletedEntry() = runTest {
        entryDao.insert(entry("e1", "EXPENSE", 1000L, "cat_food", today))
        entryDao.softDelete("e1", updatedAt = 1L)
        entryDao.restore("e1", updatedAt = 2L)

        val restored = entryDao.getById("e1")
        assertEquals(false, restored?.isDeleted)
    }

    @Test
    fun reassignCategory_movesEntriesToFallback() = runTest {
        entryDao.insert(entry("e1", "EXPENSE", 1000L, "cat_food", today))
        entryDao.insert(entry("e2", "EXPENSE", 2000L, "cat_food", today))
        entryDao.insert(entry("e3", "EXPENSE", 3000L, "cat_transport", today))

        entryDao.reassignCategory(categoryId = "cat_food", fallbackCategoryId = "cat_transport", updatedAt = 1L)

        assertEquals("cat_transport", entryDao.getById("e1")?.categoryId)
        assertEquals("cat_transport", entryDao.getById("e2")?.categoryId)
        assertEquals("cat_transport", entryDao.getById("e3")?.categoryId)
    }

    @Test
    fun categoryBreakdown_onlyIncludesExpensesForTheGivenMonth() = runTest {
        entryDao.insert(entry("e1", "EXPENSE", 1000L, "cat_food", today))
        entryDao.insert(entry("e2", "EXPENSE", 3000L, "cat_transport", today))
        entryDao.insert(entry("e3", "INCOME", 5000L, null, today))

        val start = today.withDayOfMonth(1).toEpochDay()
        val end = today.withDayOfMonth(today.lengthOfMonth()).toEpochDay()
        val breakdown = entryDao.observeCategoryBreakdown(start, end).first()

        assertEquals(2, breakdown.size)
        assertEquals(3000L, breakdown.first().totalMinor) // ORDER BY totalMinor DESC
    }
}
