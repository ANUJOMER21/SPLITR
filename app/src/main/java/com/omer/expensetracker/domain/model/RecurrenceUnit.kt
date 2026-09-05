package com.omer.expensetracker.domain.model

/** Shared by recurring entry rules and repeating bill reminders. A "monthly" rule is
 * MONTH/1, a "custom every 3 weeks" rule is WEEK/3, etc. — one model covers every
 * frequency option the spec calls for without a separate CUSTOM special case. */
enum class RecurrenceUnit {
    DAY, WEEK, MONTH
}
