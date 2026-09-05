package com.omer.expensetracker.domain.model

const val BACKUP_SCHEMA_VERSION = 1

data class BackupSummary(
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val categoryCount: Int,
    val entryCount: Int,
    val budgetCount: Int,
    val recurringRuleCount: Int,
    val reminderCount: Int,
    val goalCount: Int,
    val contributionCount: Int
)

sealed interface ImportOutcome {
    data class Preview(val summary: BackupSummary) : ImportOutcome
    data object Success : ImportOutcome
    data class Invalid(val reason: String) : ImportOutcome
}
