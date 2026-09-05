package com.omer.expensetracker.data.repository

import com.omer.expensetracker.data.local.dao.CategoryDao
import com.omer.expensetracker.data.local.dao.InsightsDao
import com.omer.expensetracker.data.local.dao.InsightsRawRow
import com.omer.expensetracker.data.mapper.toDomain
import com.omer.expensetracker.domain.model.AnomalyFlag
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.domain.model.HeatmapCell
import com.omer.expensetracker.domain.model.InsightsPeriod
import com.omer.expensetracker.domain.model.MonthOverMonthChange
import com.omer.expensetracker.domain.model.SavingsRatePoint
import com.omer.expensetracker.domain.model.TopCategoryInsight
import com.omer.expensetracker.domain.model.TrendPoint
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.repository.InsightsRepository
import com.omer.expensetracker.domain.usecase.insights.AnomalyDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

private data class PeriodRange(val label: String, val start: LocalDate, val end: LocalDate)

class InsightsRepositoryImpl @Inject constructor(
    private val insightsDao: InsightsDao,
    private val categoryDao: CategoryDao,
    private val entryRepository: EntryRepository
) : InsightsRepository {

    private fun periodRanges(period: InsightsPeriod, anchor: LocalDate): List<PeriodRange> = when (period) {
        InsightsPeriod.WEEKLY -> {
            val anchorMonday = anchor.with(DayOfWeek.MONDAY)
            (7 downTo 0).map { offset ->
                val start = anchorMonday.minusWeeks(offset.toLong())
                val end = start.plusDays(6)
                PeriodRange(start.format(DateTimeFormatter.ofPattern("dd MMM")), start, end)
            }
        }
        InsightsPeriod.MONTHLY -> {
            val anchorMonth = YearMonth.from(anchor)
            (11 downTo 0).map { offset ->
                val month = anchorMonth.minusMonths(offset.toLong())
                PeriodRange(
                    month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    month.atDay(1),
                    month.atEndOfMonth()
                )
            }
        }
        InsightsPeriod.YEARLY -> {
            (4 downTo 0).map { offset ->
                val year = anchor.year - offset
                PeriodRange(year.toString(), LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))
            }
        }
    }

    private fun observeRaw(start: LocalDate, end: LocalDate): Flow<List<InsightsRawRow>> =
        insightsDao.observeEntriesInRange(start.toEpochDay(), end.toEpochDay())

    override fun observeSpendTrend(period: InsightsPeriod, anchor: LocalDate): Flow<List<TrendPoint>> {
        val ranges = periodRanges(period, anchor)
        return observeRaw(ranges.first().start, ranges.last().end).map { rows ->
            ranges.map { range ->
                val inRange = rows.filter { it.dateEpochDay in range.start.toEpochDay()..range.end.toEpochDay() }
                TrendPoint(
                    periodLabel = range.label,
                    periodStart = range.start,
                    expenseMinor = inRange.filter { it.type == "EXPENSE" }.sumOf { it.amountMinor },
                    incomeMinor = inRange.filter { it.type == "INCOME" }.sumOf { it.amountMinor }
                )
            }
        }
    }

    override fun observeTopCategories(start: LocalDate, end: LocalDate): Flow<List<TopCategoryInsight>> =
        observeRaw(start, end).map { rows ->
            val categories = categoryDao.getAllSync().associateBy { it.id }
            val totalsByCategory = rows.filter { it.type == "EXPENSE" && it.categoryId != null }
                .groupBy { it.categoryId }
                .mapValues { (_, entries) -> entries.sumOf { it.amountMinor } }
            val grandTotal = totalsByCategory.values.sum()
            totalsByCategory.entries
                .mapNotNull { (categoryId, total) ->
                    val category = categories[categoryId] ?: return@mapNotNull null
                    TopCategoryInsight(
                        category = category.toDomain(),
                        totalMinor = total,
                        percentage = if (grandTotal > 0) total * 100f / grandTotal else 0f
                    )
                }
                .sortedByDescending { it.totalMinor }
        }

    override fun observeHeatmap(start: LocalDate, end: LocalDate): Flow<List<HeatmapCell>> =
        observeRaw(start, end).map { rows ->
            val totalsByDay = rows.filter { it.type == "EXPENSE" }
                .groupBy { LocalDate.ofEpochDay(it.dateEpochDay).dayOfWeek.value }
                .mapValues { (_, entries) -> entries.sumOf { it.amountMinor } }
            (1..7).map { dayOfWeek -> HeatmapCell(dayOfWeek, totalsByDay[dayOfWeek] ?: 0L) }
        }

    override fun observeMonthOverMonthByCategory(currentMonthStart: LocalDate): Flow<List<MonthOverMonthChange>> {
        val currentMonth = YearMonth.from(currentMonthStart)
        val previousMonth = currentMonth.minusMonths(1)
        return observeRaw(previousMonth.atDay(1), currentMonth.atEndOfMonth()).map { rows ->
            val categories = categoryDao.getAllSync().associateBy { it.id }
            val expenseRows = rows.filter { it.type == "EXPENSE" && it.categoryId != null }
            val currentByCategory = expenseRows
                .filter { it.dateEpochDay >= currentMonth.atDay(1).toEpochDay() }
                .groupBy { it.categoryId }
                .mapValues { (_, e) -> e.sumOf { it.amountMinor } }
            val previousByCategory = expenseRows
                .filter { it.dateEpochDay < currentMonth.atDay(1).toEpochDay() }
                .groupBy { it.categoryId }
                .mapValues { (_, e) -> e.sumOf { it.amountMinor } }

            (currentByCategory.keys + previousByCategory.keys).mapNotNull { categoryId ->
                val category = categories[categoryId] ?: return@mapNotNull null
                MonthOverMonthChange(
                    category = category.toDomain(),
                    currentMonthMinor = currentByCategory[categoryId] ?: 0L,
                    previousMonthMinor = previousByCategory[categoryId] ?: 0L
                )
            }.sortedByDescending { it.currentMonthMinor }
        }
    }

    override fun observeOverallMonthOverMonth(currentMonthStart: LocalDate): Flow<MonthOverMonthChange> {
        val currentMonth = YearMonth.from(currentMonthStart)
        val previousMonth = currentMonth.minusMonths(1)
        return observeRaw(previousMonth.atDay(1), currentMonth.atEndOfMonth()).map { rows ->
            val expenseRows = rows.filter { it.type == "EXPENSE" }
            val current = expenseRows.filter { it.dateEpochDay >= currentMonth.atDay(1).toEpochDay() }.sumOf { it.amountMinor }
            val previous = expenseRows.filter { it.dateEpochDay < currentMonth.atDay(1).toEpochDay() }.sumOf { it.amountMinor }
            MonthOverMonthChange(category = null, currentMonthMinor = current, previousMonthMinor = previous)
        }
    }

    override fun observeSavingsRateTrend(period: InsightsPeriod, anchor: LocalDate): Flow<List<SavingsRatePoint>> {
        val ranges = periodRanges(period, anchor)
        return observeRaw(ranges.first().start, ranges.last().end).map { rows ->
            ranges.map { range ->
                val inRange = rows.filter { it.dateEpochDay in range.start.toEpochDay()..range.end.toEpochDay() }
                SavingsRatePoint(
                    periodLabel = range.label,
                    periodStart = range.start,
                    incomeMinor = inRange.filter { it.type == "INCOME" }.sumOf { it.amountMinor },
                    expenseMinor = inRange.filter { it.type == "EXPENSE" }.sumOf { it.amountMinor }
                )
            }
        }
    }

    override fun observeAnomalies(start: LocalDate, end: LocalDate): Flow<List<AnomalyFlag>> {
        val baselineStart = start.minusDays(180)
        val filter = EntryFilter(type = EntryType.EXPENSE, startDate = baselineStart, endDate = end)
        return entryRepository.observeFilteredEntries(filter).map { entries ->
            val categories = categoryDao.getAllSync().associateBy { it.id }
            val sortedAscending = entries.sortedBy { it.date }
            val inWindow = sortedAscending.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }

            inWindow.mapNotNull { entry ->
                val categoryId = entry.categoryId ?: return@mapNotNull null
                val priorInCategory = sortedAscending.filter {
                    it.categoryId == categoryId && it.date.isBefore(entry.date)
                }
                if (priorInCategory.isEmpty()) return@mapNotNull null
                val average = priorInCategory.sumOf { it.amountMinor } / priorInCategory.size
                if (!AnomalyDetector.isAnomaly(entry.amountMinor, average, priorInCategory.size)) return@mapNotNull null
                AnomalyFlag(entry = entry, category = categories[categoryId]?.toDomain(), categoryAverageMinor = average)
            }
        }
    }
}
