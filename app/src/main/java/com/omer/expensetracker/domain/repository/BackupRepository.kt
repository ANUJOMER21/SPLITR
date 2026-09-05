package com.omer.expensetracker.domain.repository

import com.omer.expensetracker.domain.model.BackupSummary
import com.omer.expensetracker.domain.model.ImportOutcome
import java.io.InputStream
import java.io.OutputStream

interface BackupRepository {
    suspend fun export(out: OutputStream): BackupSummary
    suspend fun preview(input: InputStream): ImportOutcome
    suspend fun import(input: InputStream): ImportOutcome
}
