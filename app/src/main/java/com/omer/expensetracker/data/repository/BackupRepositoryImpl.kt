package com.omer.expensetracker.data.repository

import androidx.room.withTransaction
import com.omer.expensetracker.data.local.AppDatabase
import com.omer.expensetracker.data.local.BackupPayload
import com.omer.expensetracker.data.local.dao.BillReminderDao
import com.omer.expensetracker.data.local.dao.BudgetDao
import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.EntryDao
import com.omer.expensetracker.data.local.dao.GoalContributionDao
import com.omer.expensetracker.data.local.dao.RecurringRuleDao
import com.omer.expensetracker.data.local.dao.SavingsGoalDao
import com.omer.expensetracker.domain.model.BACKUP_SCHEMA_VERSION
import com.omer.expensetracker.domain.model.BackupSummary
import com.omer.expensetracker.domain.model.ImportOutcome
import com.omer.expensetracker.domain.repository.BackupRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val entryDao: EntryDao,
    private val budgetDao: BudgetDao,
    private val recurringRuleDao: RecurringRuleDao,
    private val billReminderDao: BillReminderDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val goalContributionDao: GoalContributionDao
) : BackupRepository {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override suspend fun export(out: OutputStream): BackupSummary {
        val payload = BackupPayload(
            schemaVersion = BACKUP_SCHEMA_VERSION,
            exportedAtEpochMillis = System.currentTimeMillis(),
            categories = categoryDao.getAllSync(),
            entries = entryDao.getAllActive(),
            budgets = budgetDao.getAllActive(),
            recurringRules = recurringRuleDao.getAllActive(),
            billReminders = billReminderDao.getAllActive(),
            savingsGoals = savingsGoalDao.getAllActive(),
            goalContributions = goalContributionDao.getAllActive()
        )
        out.writer().use { it.write(json.encodeToString(BackupPayload.serializer(), payload)) }
        return payload.toSummary()
    }

    override suspend fun preview(input: InputStream): ImportOutcome {
        val payload = parse(input) ?: return ImportOutcome.Invalid("This file isn't a valid backup.")
        val versionError = validateVersion(payload)
        if (versionError != null) return ImportOutcome.Invalid(versionError)
        return ImportOutcome.Preview(payload.toSummary())
    }

    override suspend fun import(input: InputStream): ImportOutcome {
        val payload = parse(input) ?: return ImportOutcome.Invalid("This file isn't a valid backup.")
        val versionError = validateVersion(payload)
        if (versionError != null) return ImportOutcome.Invalid(versionError)

        database.withTransaction {
            entryDao.deleteAll()
            goalContributionDao.deleteAll()
            savingsGoalDao.deleteAll()
            billReminderDao.deleteAll()
            recurringRuleDao.deleteAll()
            budgetDao.deleteAll()
            categoryDao.deleteAll()

            categoryDao.insertAll(payload.categories)
            entryDao.insertAll(payload.entries)
            budgetDao.insertAll(payload.budgets)
            recurringRuleDao.insertAll(payload.recurringRules)
            billReminderDao.insertAll(payload.billReminders)
            savingsGoalDao.insertAll(payload.savingsGoals)
            goalContributionDao.insertAll(payload.goalContributions)
        }
        return ImportOutcome.Success
    }

    private fun parse(input: InputStream): BackupPayload? = try {
        input.reader().use { json.decodeFromString(BackupPayload.serializer(), it.readText()) }
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun validateVersion(payload: BackupPayload): String? = when {
        payload.schemaVersion < 1 -> "This backup file is malformed."
        payload.schemaVersion > BACKUP_SCHEMA_VERSION ->
            "This backup was made with a newer version of the app and can't be imported here."
        else -> null
    }

    private fun BackupPayload.toSummary() = BackupSummary(
        schemaVersion = schemaVersion,
        exportedAtEpochMillis = exportedAtEpochMillis,
        categoryCount = categories.size,
        entryCount = entries.size,
        budgetCount = budgets.size,
        recurringRuleCount = recurringRules.size,
        reminderCount = billReminders.size,
        goalCount = savingsGoals.size,
        contributionCount = goalContributions.size
    )
}
