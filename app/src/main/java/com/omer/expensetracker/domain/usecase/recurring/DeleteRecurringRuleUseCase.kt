package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import javax.inject.Inject

class DeleteRecurringRuleUseCase @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository
) {
    suspend operator fun invoke(id: String) = recurringRuleRepository.deleteRule(id)
}
