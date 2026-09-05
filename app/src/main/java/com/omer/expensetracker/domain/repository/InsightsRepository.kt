package com.omer.expensetracker.domain.repository

import com.omer.expensetracker.domain.model.AnomalyFlag
import com.omer.expensetracker.domain.model.HeatmapCell
import com.omer.expensetracker.domain.model.InsightsPeriod
import com.omer.expensetracker.domain.model.MonthOverMonthChange
import com.omer.expensetracker.domain.model.SavingsRatePoint
import com.omer.expensetracker.domain.model.TopCategoryInsight
import com.omer.expensetracker.domain.model.TrendPoint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface InsightsRepository {
    fun observeSpendTrend(period: InsightsPeriod, anchor: LocalDate): Flow<List<TrendPoint>>
    fun observeTopCategories(start: LocalDate, end: LocalDate): Flow<List<TopCategoryInsight>>
    fun observeHeatmap(start: LocalDate, end: LocalDate): Flow<List<HeatmapCell>>
    fun observeMonthOverMonthByCategory(currentMonthStart: LocalDate): Flow<List<MonthOverMonthChange>>
    fun observeOverallMonthOverMonth(currentMonthStart: LocalDate): Flow<MonthOverMonthChange>
    fun observeSavingsRateTrend(period: InsightsPeriod, anchor: LocalDate): Flow<List<SavingsRatePoint>>
    fun observeAnomalies(start: LocalDate, end: LocalDate): Flow<List<AnomalyFlag>>
}
