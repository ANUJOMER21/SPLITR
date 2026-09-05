package com.omer.expensetracker.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.AnomalyFlag
import com.omer.expensetracker.domain.model.HeatmapCell
import com.omer.expensetracker.domain.model.InsightsPeriod
import com.omer.expensetracker.domain.model.MonthOverMonthChange
import com.omer.expensetracker.domain.model.SavingsRatePoint
import com.omer.expensetracker.domain.model.TopCategoryInsight
import com.omer.expensetracker.domain.model.TrendPoint
import com.omer.expensetracker.domain.usecase.insights.GetAnomaliesUseCase
import com.omer.expensetracker.domain.usecase.insights.GetHeatmapUseCase
import com.omer.expensetracker.domain.usecase.insights.GetMonthOverMonthByCategoryUseCase
import com.omer.expensetracker.domain.usecase.insights.GetOverallMonthOverMonthUseCase
import com.omer.expensetracker.domain.usecase.insights.GetSavingsRateTrendUseCase
import com.omer.expensetracker.domain.usecase.insights.GetSpendTrendUseCase
import com.omer.expensetracker.domain.usecase.insights.GetTopCategoriesInsightUseCase
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import com.omer.expensetracker.domain.repository.split.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

enum class InsightsTab { TREND, TOP_CATEGORIES, HEATMAP, INCOME_VS_EXPENSE, SAVINGS_RATE, ANOMALIES, SHARED }

data class SharedFriendBalance(val friendId: String, val name: String, val colorArgb: Long, val netMinor: Long)

data class InsightsUiState(
    val tab: InsightsTab = InsightsTab.TREND,
    val period: InsightsPeriod = InsightsPeriod.MONTHLY,
    val trend: List<TrendPoint> = emptyList(),
    val topCategories: List<TopCategoryInsight> = emptyList(),
    val heatmap: List<HeatmapCell> = emptyList(),
    val overallMoM: MonthOverMonthChange? = null,
    val categoryMoM: List<MonthOverMonthChange> = emptyList(),
    val savingsRate: List<SavingsRatePoint> = emptyList(),
    val anomalies: List<AnomalyFlag> = emptyList(),
    val sharedBalances: List<SharedFriendBalance> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    getSpendTrendUseCase: GetSpendTrendUseCase,
    getTopCategoriesInsightUseCase: GetTopCategoriesInsightUseCase,
    getHeatmapUseCase: GetHeatmapUseCase,
    getOverallMonthOverMonthUseCase: GetOverallMonthOverMonthUseCase,
    getMonthOverMonthByCategoryUseCase: GetMonthOverMonthByCategoryUseCase,
    getSavingsRateTrendUseCase: GetSavingsRateTrendUseCase,
    getAnomaliesUseCase: GetAnomaliesUseCase,
    friendRepository: FriendRepository,
    balanceRepository: BalanceRepository
) : ViewModel() {

    private val tab = MutableStateFlow(InsightsTab.TREND)
    private val period = MutableStateFlow(InsightsPeriod.MONTHLY)

    private val monthStart = YearMonth.now().atDay(1)
    private val monthEnd = YearMonth.now().atEndOfMonth()
    private val today: LocalDate = LocalDate.now()

    private val trendFlow = period.flatMapLatest { getSpendTrendUseCase(it) }
    private val savingsRateFlow = period.flatMapLatest { getSavingsRateTrendUseCase(it) }
    private val topCategoriesFlow = getTopCategoriesInsightUseCase(monthStart, monthEnd)
    private val heatmapFlow = getHeatmapUseCase(monthStart, monthEnd)
    private val overallMoMFlow = getOverallMonthOverMonthUseCase(monthStart)
    private val categoryMoMFlow = getMonthOverMonthByCategoryUseCase(monthStart)
    private val anomaliesFlow = getAnomaliesUseCase(monthStart, today)

    private data class GroupA(val trend: List<TrendPoint>, val topCategories: List<TopCategoryInsight>, val heatmap: List<HeatmapCell>)
    private data class GroupB(val overallMoM: MonthOverMonthChange, val categoryMoM: List<MonthOverMonthChange>, val savingsRate: List<SavingsRatePoint>, val anomalies: List<AnomalyFlag>)

    private val groupA = combine(trendFlow, topCategoriesFlow, heatmapFlow) { t, c, h -> GroupA(t, c, h) }
    private val groupB = combine(overallMoMFlow, categoryMoMFlow, savingsRateFlow, anomaliesFlow) { o, c, s, a -> GroupB(o, c, s, a) }
    private val sharedBalancesFlow = combine(
        friendRepository.observeFriends(), balanceRepository.observeAllOverallBalances()
    ) { friends, balances ->
        val balanceByFriendId = balances.associateBy { it.friendId }
        friends.filter { !it.isYou }.map { f ->
            SharedFriendBalance(f.id, f.name, f.avatarColorArgb, balanceByFriendId[f.id]?.netMinor ?: 0L)
        }
    }

    val uiState: StateFlow<InsightsUiState> = combine(groupA, groupB, sharedBalancesFlow, tab, period) { a, b, shared, t, p ->
        InsightsUiState(
            tab = t,
            period = p,
            trend = a.trend,
            topCategories = a.topCategories,
            heatmap = a.heatmap,
            overallMoM = b.overallMoM,
            categoryMoM = b.categoryMoM,
            savingsRate = b.savingsRate,
            anomalies = b.anomalies,
            sharedBalances = shared,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsUiState())

    fun selectTab(newTab: InsightsTab) {
        tab.value = newTab
    }

    fun selectPeriod(newPeriod: InsightsPeriod) {
        period.value = newPeriod
    }
}
