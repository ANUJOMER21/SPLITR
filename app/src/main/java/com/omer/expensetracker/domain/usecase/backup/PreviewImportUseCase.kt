package com.omer.expensetracker.domain.usecase.backup

import com.omer.expensetracker.domain.model.ImportOutcome
import com.omer.expensetracker.domain.repository.BackupRepository
import java.io.InputStream
import javax.inject.Inject

class PreviewImportUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(input: InputStream): ImportOutcome = backupRepository.preview(input)
}
