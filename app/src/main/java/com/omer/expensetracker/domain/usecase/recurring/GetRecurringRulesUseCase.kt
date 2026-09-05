package com.omer.expensetracker.domain.usecase.recurring

import com.omer.expensetracker.domain.model.RecurringRule
import com.omer.expensetracker.domain.repository.RecurringRuleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecurringRulesUseCase @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository
) {
    operator fun invoke(): Flow<List<RecurringRule>> = recurringRuleRepository.observeAll()
}
