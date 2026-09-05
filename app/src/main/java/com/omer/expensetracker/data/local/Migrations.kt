package com.omer.expensetracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Hand-written migrations for every schema bump. The DDL is copied verbatim from the exported
 * schema JSON (`app/schemas/.../<version>.json`) for the *target* version of each step, so a
 * migrated database matches what Room validates on open exactly. Room compares columns and
 * indexes by name (not order), so appending columns via `ALTER TABLE ADD COLUMN` is safe.
 */

// v1 -> v2: budgets, recurring rules, bill reminders, savings goals + contributions.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS `budgets` (`id` TEXT NOT NULL, `categoryId` TEXT, `monthlyLimitMinor` INTEGER NOT NULL, `warningNotifiedMonth` TEXT, `breachNotifiedMonth` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_budgets_categoryId` ON `budgets` (`categoryId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_budgets_isDeleted` ON `budgets` (`isDeleted`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `recurring_rules` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `categoryId` TEXT, `unit` TEXT NOT NULL, `intervalCount` INTEGER NOT NULL, `startDateEpochDay` INTEGER NOT NULL, `endDateEpochDay` INTEGER, `lastGeneratedDateEpochDay` INTEGER, `nextDueDateEpochDay` INTEGER NOT NULL, `isPaused` INTEGER NOT NULL, `skipNextOccurrence` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_recurring_rules_categoryId` ON `recurring_rules` (`categoryId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_recurring_rules_nextDueDateEpochDay` ON `recurring_rules` (`nextDueDateEpochDay`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_recurring_rules_isDeleted` ON `recurring_rules` (`isDeleted`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `bill_reminders` (`id` TEXT NOT NULL, `label` TEXT NOT NULL, `estimatedAmountMinor` INTEGER, `categoryId` TEXT, `dueDateEpochDay` INTEGER NOT NULL, `leadDays` INTEGER NOT NULL, `repeatUnit` TEXT, `repeatIntervalCount` INTEGER, `isPaid` INTEGER NOT NULL, `paidEntryId` TEXT, `lastNotifiedForDueDateEpochDay` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`paidEntryId`) REFERENCES `entries`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_bill_reminders_categoryId` ON `bill_reminders` (`categoryId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_bill_reminders_paidEntryId` ON `bill_reminders` (`paidEntryId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_bill_reminders_dueDateEpochDay` ON `bill_reminders` (`dueDateEpochDay`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_bill_reminders_isDeleted` ON `bill_reminders` (`isDeleted`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `savings_goals` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `targetAmountMinor` INTEGER NOT NULL, `targetDateEpochDay` INTEGER, `isCompleted` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_savings_goals_isDeleted` ON `savings_goals` (`isDeleted`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `goal_contributions` (`id` TEXT NOT NULL, `goalId` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `dateEpochDay` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`goalId`) REFERENCES `savings_goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_goal_contributions_goalId` ON `goal_contributions` (`goalId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_goal_contributions_isDeleted` ON `goal_contributions` (`isDeleted`)""")
    }
}

// v2 -> v3: the whole Split-With-Friends table set.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS `friends` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `contactInfo` TEXT, `avatarColorArgb` INTEGER NOT NULL, `isYou` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_friends_isDeleted` ON `friends` (`isDeleted`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `friend_groups` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `iconKey` TEXT NOT NULL, `colorArgb` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_friend_groups_isDeleted` ON `friend_groups` (`isDeleted`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_friend_groups_isArchived` ON `friend_groups` (`isArchived`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `group_members` (`groupId` TEXT NOT NULL, `friendId` TEXT NOT NULL, `joinedAt` INTEGER NOT NULL, PRIMARY KEY(`groupId`, `friendId`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_group_members_friendId` ON `group_members` (`friendId`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `shared_expenses` (`id` TEXT NOT NULL, `description` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `categoryId` TEXT, `splitType` TEXT NOT NULL, `dateEpochDay` INTEGER NOT NULL, `groupId` TEXT, `photoUri` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_shared_expenses_groupId` ON `shared_expenses` (`groupId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_shared_expenses_categoryId` ON `shared_expenses` (`categoryId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_shared_expenses_isDeleted` ON `shared_expenses` (`isDeleted`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_shared_expenses_dateEpochDay` ON `shared_expenses` (`dateEpochDay`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `expense_payers` (`id` TEXT NOT NULL, `expenseId` TEXT NOT NULL, `friendId` TEXT NOT NULL, `paidAmountMinor` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_expense_payers_expenseId` ON `expense_payers` (`expenseId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_expense_payers_friendId` ON `expense_payers` (`friendId`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `expense_splits` (`id` TEXT NOT NULL, `expenseId` TEXT NOT NULL, `friendId` TEXT NOT NULL, `owedAmountMinor` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_expense_splits_expenseId` ON `expense_splits` (`expenseId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_expense_splits_friendId` ON `expense_splits` (`friendId`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `expense_items` (`id` TEXT NOT NULL, `expenseId` TEXT NOT NULL, `name` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_expense_items_expenseId` ON `expense_items` (`expenseId`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `expense_item_shares` (`itemId` TEXT NOT NULL, `friendId` TEXT NOT NULL, PRIMARY KEY(`itemId`, `friendId`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_expense_item_shares_friendId` ON `expense_item_shares` (`friendId`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `expense_comments` (`id` TEXT NOT NULL, `expenseId` TEXT NOT NULL, `text` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_expense_comments_expenseId` ON `expense_comments` (`expenseId`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `expense_attachments` (`id` TEXT NOT NULL, `expenseId` TEXT NOT NULL, `uri` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_expense_attachments_expenseId` ON `expense_attachments` (`expenseId`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `balances` (`id` TEXT NOT NULL, `friendId` TEXT NOT NULL, `groupId` TEXT, `netMinor` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_balances_friendId` ON `balances` (`friendId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_balances_groupId` ON `balances` (`groupId`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `settlements` (`id` TEXT NOT NULL, `payerFriendId` TEXT NOT NULL, `receiverFriendId` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `dateEpochDay` INTEGER NOT NULL, `note` TEXT, `groupId` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_settlements_payerFriendId` ON `settlements` (`payerFriendId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_settlements_receiverFriendId` ON `settlements` (`receiverFriendId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_settlements_groupId` ON `settlements` (`groupId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_settlements_isDeleted` ON `settlements` (`isDeleted`)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `activity_log` (`id` TEXT NOT NULL, `groupId` TEXT, `type` TEXT NOT NULL, `summary` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_activity_log_groupId` ON `activity_log` (`groupId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_activity_log_timestamp` ON `activity_log` (`timestamp`)""")
    }
}

// v3 -> v4: cloud-sync outbox.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS `sync_queue` (`id` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `operation` TEXT NOT NULL, `payloadJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `attempts` INTEGER NOT NULL, `lastError` TEXT, PRIMARY KEY(`id`))""")
    }
}

// v4 -> v5: friend email + linked account uid.
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE friends ADD COLUMN email TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE friends ADD COLUMN linkedUserId TEXT")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_friends_linkedUserId` ON `friends` (`linkedUserId`)""")
    }
}

// v5 -> v6: entry <-> shared-expense link.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE entries ADD COLUMN linkedSharedExpenseId TEXT")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_entries_linkedSharedExpenseId` ON `entries` (`linkedSharedExpenseId`)""")
    }
}

// v6 -> v7: multi-group settlement batch id.
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE settlements ADD COLUMN batchId TEXT")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_settlements_batchId` ON `settlements` (`batchId`)""")
    }
}

// v7 -> v8: entry <-> savings-goal-contribution link.
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE entries ADD COLUMN linkedGoalContributionId TEXT")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_entries_linkedGoalContributionId` ON `entries` (`linkedGoalContributionId`)""")
    }
}

// v8 -> v9: entry note.
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE entries ADD COLUMN note TEXT")
    }
}

// v9 -> v10: entry receipt photo.
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE entries ADD COLUMN photoUri TEXT")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_entries_photoUri` ON `entries` (`photoUri`)""")
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
    MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10
)
