package com.omer.expensetracker.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.omer.expensetracker.domain.service.WidgetRefresher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GlanceWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetRefresher {
    override suspend fun refreshAll() {
        SummaryWidget().updateAll(context)
        RecentTransactionsWidget().updateAll(context)
    }
}
