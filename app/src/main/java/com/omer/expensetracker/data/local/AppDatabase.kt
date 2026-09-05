package com.omer.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.omer.expensetracker.data.local.entity.BillReminderEntity
import com.omer.expensetracker.data.local.entity.BudgetEntity
import com.omer.expensetracker.data.local.entity.CategoryEntity
import com.omer.expensetracker.data.local.entity.EntryEntity
import com.omer.expensetracker.data.local.entity.GoalContributionEntity
import com.omer.expensetracker.data.local.entity.RecurringRuleEntity
import com.omer.expensetracker.data.local.entity.SavingsGoalEntity
import com.omer.expensetracker.data.local.entity.split.ActivityLogEntity
import com.omer.expensetracker.data.local.entity.split.BalanceEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseAttachmentEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseCommentEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseItemEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseItemShareEntity
import com.omer.expensetracker.data.local.entity.split.ExpensePayerEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseSplitEntity
import com.omer.expensetracker.data.local.entity.split.FriendEntity
import com.omer.expensetracker.data.local.entity.split.GroupEntity
import com.omer.expensetracker.data.local.entity.split.GroupMemberEntity
import com.omer.expensetracker.data.local.entity.split.SettlementEntity
import com.omer.expensetracker.data.local.entity.split.SharedExpenseEntity
import com.omer.expensetracker.data.local.entity.sync.SyncQueueEntity

@Database(
    entities = [
        CategoryEntity::class, EntryEntity::class,
        BudgetEntity::class, RecurringRuleEntity::class, BillReminderEntity::class,
        SavingsGoalEntity::class, GoalContributionEntity::class,
        // Split With Friends (Phase 3) — built in full but gated off by
        // com.omer.expensetracker.FeatureFlags.SPLIT_WITH_FRIENDS_ENABLED until it ships.
        FriendEntity::class, GroupEntity::class, GroupMemberEntity::class,
        SharedExpenseEntity::class, ExpensePayerEntity::class, ExpenseSplitEntity::class,
        ExpenseItemEntity::class, ExpenseItemShareEntity::class,
        ExpenseCommentEntity::class, ExpenseAttachmentEntity::class,
        BalanceEntity::class, SettlementEntity::class, ActivityLogEntity::class,
        // Phase 5 cloud sync scaffold — see FeatureFlags.CLOUD_SYNC_ENABLED.
        SyncQueueEntity::class
    ],
    version = 10,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun entryDao(): EntryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringRuleDao(): RecurringRuleDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun goalContributionDao(): GoalContributionDao
    abstract fun insightsDao(): InsightsDao

    abstract fun friendDao(): FriendDao
    abstract fun groupDao(): GroupDao
    abstract fun sharedExpenseDao(): SharedExpenseDao
    abstract fun balanceDao(): BalanceDao
    abstract fun settlementDao(): SettlementDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun commentAttachmentDao(): CommentAttachmentDao

    abstract fun syncQueueDao(): SyncQueueDao

    /**
     * Seeds the default categories, and the implicit "You" friend record, exactly once: this
     * callback only fires the moment the database file is first created, so re-launching the
     * app never re-inserts them. Uses raw SQL (not the DAO) since the DAO isn't reachable yet
     * during database construction.
     */
    object SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val now = System.currentTimeMillis()
            db.beginTransaction()
            try {
                DEFAULT_CATEGORY_SEEDS.forEach { seed ->
                    db.execSQL(
                        """
                        INSERT INTO categories (id, name, iconKey, colorArgb, isDefault, isActive, createdAt, updatedAt)
                        VALUES (?, ?, ?, ?, 1, 1, ?, ?)
                        """.trimIndent(),
                        arrayOf(seed.id, seed.name, seed.iconKey, seed.colorArgb, now, now)
                    )
                }
                db.execSQL(
                    """
                    INSERT INTO friends (id, name, email, contactInfo, avatarColorArgb, isYou, linkedUserId, createdAt, updatedAt, isDeleted)
                    VALUES (?, ?, '', NULL, ?, 1, NULL, ?, ?, 0)
                    """.trimIndent(),
                    arrayOf("you", "You", 0xFF3B82F6L, now, now)
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
