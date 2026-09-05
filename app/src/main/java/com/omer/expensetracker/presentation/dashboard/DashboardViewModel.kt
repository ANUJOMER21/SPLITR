package com.omer.expensetracker.presentation.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.usecase.GetCategoryBreakdownUseCase
import com.omer.expensetracker.domain.usecase.GetMonthlySummaryUseCase
import com.omer.expensetracker.domain.usecase.budget.GetBudgetsWithProgressUseCase
import com.omer.expensetracker.domain.usecase.insights.GetOverallMonthOverMonthUseCase
import com.omer.expensetracker.domain.repository.CategoryRepository
import com.omer.expensetracker.domain.repository.EntryRepository
import com.omer.expensetracker.domain.repository.split.BalanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth
import javax.inject.Inject

private const val KEY_MONTH = "dashboard_month"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getMonthlySummaryUseCase: GetMonthlySummaryUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    private val getBudgetsWithProgressUseCase: GetBudgetsWithProgressUseCase,
    private val getOverallMonthOverMonthUseCase: GetOverallMonthOverMonthUseCase,
    private val balanceRepository: BalanceRepository,
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(
        savedStateHandle.get<String>(KEY_MONTH)?.let { YearMonth.parse(it) } ?: YearMonth.now()
    )

    private val monthlyState = selectedMonth
        .flatMapLatest { month ->
            combine(
                getMonthlySummaryUseCase(month),
                getCategoryBreakdownUseCase(month),
                getBudgetsWithProgressUseCase(month),
                getOverallMonthOverMonthUseCase(month.atDay(1))
            ) { summary, breakdown, budgets, mom ->
                DashboardUiState(
                    month = month,
                    summary = summary,
                    breakdown = breakdown,
                    budgets = budgets,
                    overallMoM = mom,
                    isLoading = false
                )
            }
        }

    private val recentEntries = entryRepository.observeFilteredEntries(EntryFilter()).map { it.take(5) }
    private val categoriesById = categoryRepository.observeAllCategories().map { list -> list.associateBy { it.id } }

    val uiState: StateFlow<DashboardUiState> = combine(
        monthlyState, balanceRepository.observeAllOverallBalances(), recentEntries, categoriesById
    ) { state, balances, entries, catsById ->
        state.copy(
            sharedNetMinor = balances.sumOf { it.netMinor },
            sharedFriendCount = balances.count { it.netMinor != 0L },
            recentEntries = entries,
            categoriesById = catsById
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun previousMonth() = changeMonth { it.minusMonths(1) }
    fun nextMonth() = changeMonth { it.plusMonths(1) }

    private fun changeMonth(transform: (YearMonth) -> YearMonth) {
        selectedMonth.update { current ->
            val next = transform(current)
            savedStateHandle[KEY_MONTH] = next.toString()
            next
        }
    }
}
