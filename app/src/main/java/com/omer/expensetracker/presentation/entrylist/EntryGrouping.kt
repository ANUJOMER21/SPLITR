package com.omer.expensetracker.presentation.entrylist

import com.omer.expensetracker.domain.model.Entry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Entries already arrive sorted newest-first from the DAO; grouping preserves that order
 * and only ever needs a single linear pass. */
fun groupEntriesByDate(entries: List<Entry>, today: LocalDate = LocalDate.now()): List<EntrySection> {
    val yesterday = today.minusDays(1)
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    return entries
        .groupBy { it.date }
        .toSortedMap(compareByDescending { it })
        .map { (date, entriesForDate) ->
            val label = when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> date.format(formatter)
            }
            EntrySection(label, entriesForDate)
        }
}
