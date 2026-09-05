package com.omer.expensetracker.widget

import com.omer.expensetracker.domain.usecase.AddExpenseUseCase
import com.omer.expensetracker.domain.usecase.AddIncomeUseCase
import com.omer.expensetracker.domain.usecase.GetFilteredEntriesUseCase
import com.omer.expensetracker.domain.usecase.GetMonthlySummaryUseCase
import com.omer.expensetracker.domain.usecase.category.GetActiveCategoriesUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import android.content.Context

/** Glance widgets and the quick-add trampoline activity aren't Hilt injection targets
 * (they're not Activities/Fragments Hilt can annotate the usual way for widgets, and the
 * quick-add activity is intentionally lightweight) — this EntryPoint is how they reach the
 * same use cases the rest of the app uses, so "add from the widget" and "add from the app"
 * are the exact same code path. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getMonthlySummaryUseCase(): GetMonthlySummaryUseCase
    fun getFilteredEntriesUseCase(): GetFilteredEntriesUseCase
    fun getActiveCategoriesUseCase(): GetActiveCategoriesUseCase
    fun addExpenseUseCase(): AddExpenseUseCase
    fun addIncomeUseCase(): AddIncomeUseCase
}

fun Context.widgetEntryPoint(): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
