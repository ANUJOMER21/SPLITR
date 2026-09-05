package com.omer.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.omer.expensetracker.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/** Swaps the real file-backed database for an in-memory one during instrumented tests,
 * so tests never touch or depend on state left over from a previous run. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideInMemoryAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(AppDatabase.SeedCallback)
            .build()

    @Provides
    fun provideCategoryDao(database: AppDatabase) = database.categoryDao()

    @Provides
    fun provideEntryDao(database: AppDatabase) = database.entryDao()

    @Provides
    fun provideBudgetDao(database: AppDatabase) = database.budgetDao()

    @Provides
    fun provideRecurringRuleDao(database: AppDatabase) = database.recurringRuleDao()

    @Provides
    fun provideBillReminderDao(database: AppDatabase) = database.billReminderDao()

    @Provides
    fun provideSavingsGoalDao(database: AppDatabase) = database.savingsGoalDao()

    @Provides
    fun provideGoalContributionDao(database: AppDatabase) = database.goalContributionDao()

    @Provides
    fun provideInsightsDao(database: AppDatabase) = database.insightsDao()
}
