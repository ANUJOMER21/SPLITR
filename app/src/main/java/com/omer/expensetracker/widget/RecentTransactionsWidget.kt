package com.omer.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.omer.expensetracker.MainActivity
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.presentation.util.formatAsCurrency
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter

private const val MAX_ROWS = 6

class RecentTransactionsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = context.widgetEntryPoint()
        val entries = entryPoint.getFilteredEntriesUseCase()(EntryFilter()).first().take(MAX_ROWS)
        val categoriesById = entryPoint.getActiveCategoriesUseCase()().first().associateBy { it.id }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.Background)
                    .cornerRadius(20.dp)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent",
                        style = TextStyle(color = WidgetColors.OnSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
                Spacer(modifier = GlanceModifier.height(4.dp))

                if (entries.isEmpty()) {
                    Text(
                        "No entries yet",
                        style = TextStyle(color = WidgetColors.OnSurfaceMuted, fontSize = 13.sp)
                    )
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        items(entries, itemId = { it.id.hashCode().toLong() }) { entry ->
                            val categoryName = if (entry.type == EntryType.EXPENSE) {
                                categoriesById[entry.categoryId]?.name ?: "Other"
                            } else {
                                "Income"
                            }
                            val sign = if (entry.type == EntryType.EXPENSE) "-" else "+"
                            val color = if (entry.type == EntryType.EXPENSE) WidgetColors.Expense else WidgetColors.Income

                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = GlanceModifier.defaultWeight()) {
                                    Text(
                                        categoryName,
                                        style = TextStyle(color = WidgetColors.OnSurface, fontSize = 13.sp)
                                    )
                                    Text(
                                        entry.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                                        style = TextStyle(color = WidgetColors.OnSurfaceMuted, fontSize = 11.sp)
                                    )
                                }
                                Text(
                                    "$sign${entry.amountMinor.formatAsCurrency()}",
                                    style = TextStyle(color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class RecentTransactionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RecentTransactionsWidget()
}
