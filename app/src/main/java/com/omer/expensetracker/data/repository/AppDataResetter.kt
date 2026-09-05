package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.AppDatabase
import com.omer.expensetracker.data.local.DatabaseSeeder
import com.omer.expensetracker.domain.service.WidgetRefresher
import javax.inject.Inject

/**
 * Wipes every local table and re-seeds the defaults (categories + the implicit "You" friend).
 * Does not touch DataStore preferences or the signed-in account. Cloud-synced Split data will
 * re-pull from Firestore on the next sync; purely local data (entries, budgets, goals,
 * recurring, reminders) is gone for good.
 */
class AppDataResetter @Inject constructor(
    private val db: AppDatabase,
    private val databaseSeeder: DatabaseSeeder,
    private val widgetRefresher: WidgetRefresher
) {
    suspend fun reset() {
        db.clearAllTables()
        databaseSeeder.ensureSeeded()
        widgetRefresher.refreshAll()
    }
}
