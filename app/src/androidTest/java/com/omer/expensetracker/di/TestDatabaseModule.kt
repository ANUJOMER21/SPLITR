package com.omer.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.omer.expensetracker.data.local.AppDatabase
import com.omer.expensetracker.data.local.dao.BillReminderDao
import com.omer.expensetracker.data.local.dao.BudgetDao
import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.local.dao.GoalContributionDao
import com.omer.expensetracker.data.local.dao.InsightsDao
import com.omer.expensetracker.data.local.dao.RecurringRuleDao
import com.omer.expensetracker.data.local.dao.SavingsGoalDao
import com.omer.expensetracker.data.local.dao.split.ActivityLogDao
import com.omer.expensetracker.data.local.dao.split.BalanceDao
import com.omer.expensetracker.data.local.dao.split.CommentAttachmentDao
import com.omer.expensetracker.data.local.dao.split.FriendDao
import com.omer.expensetracker.data.local.dao.split.GroupDao
import com.omer.expensetracker.data.local.dao.split.SettlementDao
import com.omer.expensetracker.data.local.dao.split.SharedExpenseDao
import com.omer.expensetracker.data.local.dao.sync.SyncQueueDao
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
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideEntryDao(database: AppDatabase): EntryDao = database.entryDao()

    @Provides
    fun provideBudgetDao(database: AppDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideRecurringRuleDao(database: AppDatabase): RecurringRuleDao = database.recurringRuleDao()

    @Provides
    fun provideBillReminderDao(database: AppDatabase): BillReminderDao = database.billReminderDao()

    @Provides
    fun provideSavingsGoalDao(database: AppDatabase): SavingsGoalDao = database.savingsGoalDao()

    @Provides
    fun provideGoalContributionDao(database: AppDatabase): GoalContributionDao = database.goalContributionDao()

    @Provides
    fun provideInsightsDao(database: AppDatabase): InsightsDao = database.insightsDao()

    @Provides
    fun provideFriendDao(database: AppDatabase): FriendDao = database.friendDao()

    @Provides
    fun provideGroupDao(database: AppDatabase): GroupDao = database.groupDao()

    @Provides
    fun provideSharedExpenseDao(database: AppDatabase): SharedExpenseDao = database.sharedExpenseDao()

    @Provides
    fun provideBalanceDao(database: AppDatabase): BalanceDao = database.balanceDao()

    @Provides
    fun provideSettlementDao(database: AppDatabase): SettlementDao = database.settlementDao()

    @Provides
    fun provideActivityLogDao(database: AppDatabase): ActivityLogDao = database.activityLogDao()

    @Provides
    fun provideCommentAttachmentDao(database: AppDatabase): CommentAttachmentDao = database.commentAttachmentDao()

    @Provides
    fun provideSyncQueueDao(database: AppDatabase): SyncQueueDao = database.syncQueueDao()
}
