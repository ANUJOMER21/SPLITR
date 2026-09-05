package com.omer.expensetracker.domain.model

/** categoryId null means the overall monthly spend cap, independent of per-category budgets. */
data class Budget(
    val id: String,
    val categoryId: String?,
    val monthlyLimitMinor: Long,
    val warningNotifiedMonth: String?,
    val breachNotifiedMonth: String?,
    val createdAt: Long,
    val updatedAt: Long
)

enum class BudgetStatus { SAFE, WARNING, BREACHED }

const val BUDGET_WARNING_THRESHOLD = 0.8f

data class BudgetProgress(
    val budget: Budget,
    val category: Category?,
    val spentMinor: Long,
    val limitMinor: Long
) {
    val fraction: Float get() = if (limitMinor > 0) spentMinor.toFloat() / limitMinor else 0f
    val status: BudgetStatus
        get() = when {
            spentMinor >= limitMinor -> BudgetStatus.BREACHED
            fraction >= BUDGET_WARNING_THRESHOLD -> BudgetStatus.WARNING
            else -> BudgetStatus.SAFE
        }
}

/** One row of the budget history view: the limit that was in effect for [month] versus
 * what was actually spent that month. */
data class BudgetMonthSnapshot(
    val budget: Budget,
    val category: Category?,
    val limitMinorThatMonth: Long,
    val spentMinor: Long
)
