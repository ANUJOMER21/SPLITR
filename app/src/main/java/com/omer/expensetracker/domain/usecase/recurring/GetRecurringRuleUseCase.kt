package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import javax.inject.Inject

class GetRecurringRuleUseCase @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository
) {
    suspend operator fun invoke(id: String): RecurringRule? = recurringRuleRepository.getById(id)
}
