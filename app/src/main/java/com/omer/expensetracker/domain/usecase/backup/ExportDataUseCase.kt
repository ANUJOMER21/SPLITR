package com.omer.expensetracker.domain.usecase.backup

import com.omer.expensetracker.domain.model.BackupSummary
import com.omer.expensetracker.domain.repository.BackupRepository
import java.io.OutputStream
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(out: OutputStream): BackupSummary = backupRepository.export(out)
}
