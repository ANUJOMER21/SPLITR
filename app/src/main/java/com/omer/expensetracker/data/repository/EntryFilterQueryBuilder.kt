package com.omer.expensetracker.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.omer.expensetracker.domain.model.EntryFilter

/** Builds the dynamic, AND-combined filter query for the entry list, using bind args
 * throughout (never string-concatenated values) to stay injection-safe. */
internal object EntryFilterQueryBuilder {

    fun build(filter: EntryFilter): SupportSQLiteQuery {
        val where = StringBuilder("entries.isDeleted = 0")
        val args = mutableListOf<Any>()

        filter.type?.let {
            where.append(" AND entries.type = ?")
            args.add(it.name)
        }
        if (filter.categoryIds.isNotEmpty()) {
            val placeholders = filter.categoryIds.joinToString(",") { "?" }
            where.append(" AND entries.categoryId IN ($placeholders)")
            args.addAll(filter.categoryIds)
        }
        filter.startDate?.let {
            where.append(" AND entries.dateEpochDay >= ?")
            args.add(it.toEpochDay())
        }
        filter.endDate?.let {
            where.append(" AND entries.dateEpochDay <= ?")
            args.add(it.toEpochDay())
        }
        filter.minAmountMinor?.let {
            where.append(" AND entries.amountMinor >= ?")
            args.add(it)
        }
        filter.maxAmountMinor?.let {
            where.append(" AND entries.amountMinor <= ?")
            args.add(it)
        }
        val query = filter.searchQuery.trim()
        if (query.isNotEmpty()) {
            where.append(
                """
                 AND (
                    entries.categoryId IN (SELECT id FROM categories WHERE name LIKE ? COLLATE NOCASE)
                    OR CAST(entries.amountMinor / 100.0 AS TEXT) LIKE ?
                 )
                """.trimIndent()
            )
            args.add("%$query%")
            args.add("%$query%")
        }

        val sql = """
            SELECT entries.* FROM entries
            WHERE $where
            ORDER BY entries.dateEpochDay DESC, entries.createdAt DESC
        """.trimIndent()

        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }
}
