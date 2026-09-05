package com.omer.expensetracker.di

import com.omer.expensetracker.data.repository.BackupRepositoryImpl
import com.omer.expensetracker.data.repository.split.ActivityLogRepositoryImpl
import com.omer.expensetracker.data.repository.split.BalanceRepositoryImpl
import com.omer.expensetracker.data.repository.split.FriendRepositoryImpl
import com.omer.expensetracker.data.repository.split.GroupRepositoryImpl
import com.omer.expensetracker.data.repository.split.SettlementRepositoryImpl
import com.omer.expensetracker.data.repository.split.SharedExpenseRepositoryImpl
import com.omer.expensetracker.data.repository.sync.FirebaseAuthRepositoryImpl
import com.omer.expensetracker.data.repository.sync.FirebaseCloudSyncRepositoryImpl
import com.omer.expensetracker.data.repository.sync.FirestoreReminderRepositoryImpl
import com.omer.expensetracker.data.repository.sync.FirestoreUserDirectoryRepositoryImpl
import com.omer.expensetracker.data.repository.BillReminderRepositoryImpl
import com.omer.expensetracker.data.repository.BudgetRepositoryImpl
import com.omer.expensetracker.data.repository.CategoryRepositoryImpl
import com.omer.expensetracker.data.repository.EntryRepositoryImpl
import com.omer.expensetracker.data.repository.InsightsRepositoryImpl
import com.omer.expensetracker.data.repository.RecurringRuleRepositoryImpl
import com.omer.expensetracker.data.repository.SavingsGoalRepositoryImpl
import com.omer.expensetracker.domain.repository.BackupRepository
import com.omer.expensetracker.domain.repository.BillReminderRepository
import com.omer.expensetracker.domain.repository.BudgetRepository
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.repository.InsightsRepository
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import com.omer.expensetracker.domain.repository.SavingsGoalRepository
import com.omer.expensetracker.domain.repository.split.ActivityLogRepository
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import com.omer.expensetracker.domain.repository.split.FriendRepository
import com.omer.expensetracker.domain.repository.split.GroupRepository
import com.omer.expensetracker.domain.repository.split.SettlementRepository
import com.omer.expensetracker.domain.repository.split.SharedExpenseRepository
import com.omer.expensetracker.domain.repository.sync.AuthRepository
import com.omer.expensetracker.domain.repository.sync.CloudSyncRepository
import com.omer.expensetracker.domain.repository.sync.ReminderRepository
import com.omer.expensetracker.domain.repository.sync.UserDirectoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: com.omer.expensetracker.data.repository.SettingsRepositoryImpl): com.omer.expensetracker.domain.repository.SettingsRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindEntryRepository(impl: EntryRepositoryImpl): EntryRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindRecurringRuleRepository(impl: RecurringRuleRepositoryImpl): RecurringRuleRepository

    @Binds
    @Singleton
    abstract fun bindBillReminderRepository(impl: BillReminderRepositoryImpl): BillReminderRepository

    @Binds
    @Singleton
    abstract fun bindSavingsGoalRepository(impl: SavingsGoalRepositoryImpl): SavingsGoalRepository

    @Binds
    @Singleton
    abstract fun bindInsightsRepository(impl: InsightsRepositoryImpl): InsightsRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository

    // ---- Split With Friends (Phase 3) — see FeatureFlags.SPLIT_WITH_FRIENDS_ENABLED ----

    @Binds
    @Singleton
    abstract fun bindFriendRepository(impl: FriendRepositoryImpl): FriendRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun bindSharedExpenseRepository(impl: SharedExpenseRepositoryImpl): SharedExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBalanceRepository(impl: BalanceRepositoryImpl): BalanceRepository

    @Binds
    @Singleton
    abstract fun bindSettlementRepository(impl: SettlementRepositoryImpl): SettlementRepository

    @Binds
    @Singleton
    abstract fun bindActivityLogRepository(impl: ActivityLogRepositoryImpl): ActivityLogRepository

    // ---- Cloud sync (Phase 5) — see FeatureFlags.CLOUD_SYNC_ENABLED ----

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCloudSyncRepository(impl: FirebaseCloudSyncRepositoryImpl): CloudSyncRepository

    @Binds
    @Singleton
    abstract fun bindUserDirectoryRepository(impl: FirestoreUserDirectoryRepositoryImpl): UserDirectoryRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: FirestoreReminderRepositoryImpl): ReminderRepository
}
