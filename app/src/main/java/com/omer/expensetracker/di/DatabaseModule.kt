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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "expense_tracker.db")
            .addCallback(AppDatabase.SeedCallback)
            // Pre-release app, no deployed user data yet — Phase 2's new tables land via a
            // clean rebuild rather than a hand-written migration. Revisit before any real
            // release: at that point this must become a real Migration(1, 2).
            .fallbackToDestructiveMigration(true)
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
