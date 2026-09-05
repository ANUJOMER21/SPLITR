package com.omer.expensetracker.data.local

import com.omer.expensetracker.data.local.entity.BillReminderEntity
import com.omer.expensetracker.data.local.entity.BudgetEntity
import com.omer.expensetracker.data.local.entity.CategoryEntity
import com.omer.expensetracker.data.local.entity.EntryEntity
import com.omer.expensetracker.data.local.entity.GoalContributionEntity
import com.omer.expensetracker.data.local.entity.RecurringRuleEntity
import com.omer.expensetracker.data.local.entity.SavingsGoalEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val categories: List<CategoryEntity>,
    val entries: List<EntryEntity>,
    val budgets: List<BudgetEntity>,
    val recurringRules: List<RecurringRuleEntity>,
    val billReminders: List<BillReminderEntity>,
    val savingsGoals: List<SavingsGoalEntity>,
    val goalContributions: List<GoalContributionEntity>
)
