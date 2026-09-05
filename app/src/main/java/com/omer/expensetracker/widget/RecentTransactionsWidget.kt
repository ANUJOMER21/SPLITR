package com.omer.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.omer.expensetracker.MainActivity
import com.omer.expensetracker.R
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.presentation.util.formatAsCurrency
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter

private const val MAX_ROWS = 6

/** Recent transaction feed, sharing the same dark gradient identity and glass-chip styling
 * (see [WidgetTheme]) as [BalanceWidget]. */
class RecentTransactionsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = context.widgetEntryPoint()
        val allEntries = entryPoint.getFilteredEntriesUseCase()(EntryFilter()).first()
        val entries = allEntries.take(MAX_ROWS)
        val hasMore = allEntries.size > entries.size
        val categoriesById = entryPoint.getActiveCategoriesUseCase()().first().associateBy { it.id }
        val openApp = actionStartActivity(Intent(context, MainActivity::class.java))

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.widget_gradient_background))
                    .padding(18.dp)
            ) {
                TransactionsHeader(onOpenApp = openApp, onRefresh = actionRunCallback<RefreshTransactionsAction>())
                Spacer(modifier = GlanceModifier.height(14.dp))

                if (entries.isEmpty()) {
                    EmptyState(icon = "+", title = "No entries yet", subtitle = "Log an expense to see it here")
                } else {
                    // fillMaxWidth + weight instead of fillMaxSize, so there's room left for
                    // the "See all" footer below when there are more entries than fit.
                    LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                        itemsIndexed(entries, itemId = { _, item -> item.id.hashCode().toLong() }) { index, entry ->
                            val category = if (entry.type == EntryType.EXPENSE) categoriesById[entry.categoryId] else null
                            val categoryName = category?.name ?: if (entry.type == EntryType.EXPENSE) "Other" else "Income"
                            val glyphColor = when {
                                entry.type == EntryType.INCOME -> WidgetColors.Income
                                category != null -> ColorProvider(Color(category.colorArgb.toInt()))
                                else -> WidgetTheme.TextTertiary
                            }
                            val sign = if (entry.type == EntryType.EXPENSE) "-" else "+"
                            val amountColor = if (entry.type == EntryType.EXPENSE) WidgetColors.Expense else WidgetColors.Income

                            Column {
                                Row(
                                    // Each row (not the outer container) gets its own clickable,
                                    // since LazyColumn renders as a scrolling ListView under the
                                    // hood and reliably eats touch events meant for anything
                                    // behind it — an outer-level click won't fire here.
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .clickable(openApp)
                                        .background(WidgetTheme.RowBackground)
                                        .cornerRadius(14.dp)
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = GlanceModifier.size(32.dp).background(WidgetTheme.ChipBackground).cornerRadius(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            categoryName.take(1).uppercase(),
                                            style = TextStyle(color = glyphColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = GlanceModifier.width(10.dp))
                                    Column(modifier = GlanceModifier.defaultWeight()) {
                                        Text(
                                            categoryName,
                                            style = TextStyle(color = WidgetTheme.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                            maxLines = 1
                                        )
                                        Text(
                                            entry.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                                            style = TextStyle(color = WidgetTheme.TextTertiary, fontSize = 11.sp),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        "$sign${entry.amountMinor.formatAsCurrency()}",
                                        style = TextStyle(color = amountColor, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                                if (index != entries.lastIndex) {
                                    Spacer(modifier = GlanceModifier.height(6.dp))
                                }
                            }
                        }
                    }

                    if (hasMore) {
                        Text(
                            "See all →",
                            style = TextStyle(color = WidgetTheme.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            modifier = GlanceModifier
                                .clickable(openApp)
                                .padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsHeader(onOpenApp: Action, onRefresh: Action) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().clickable(onOpenApp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier.size(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.logo),
                contentDescription = null,
                modifier = GlanceModifier.size(30.dp).cornerRadius(5.dp)
            )
        }
        Spacer(modifier = GlanceModifier.width(10.dp))
        Text(
            "RECENT ACTIVITY",
            style = TextStyle(color = WidgetTheme.TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
        Box(
            modifier = GlanceModifier
                .size(26.dp)
                .background(WidgetTheme.ChipBackground)
                .cornerRadius(13.dp)
                .clickable(onRefresh),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.reload_svgrepo_com),
                contentDescription = "Refresh",
                modifier = GlanceModifier.size(14.dp)
            )
        }
    }
}

/** Manually refreshes the widget's data without waiting for the system's update cycle. */
class RefreshTransactionsAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        RecentTransactionsWidget().update(context, glanceId)
    }
}

class RecentTransactionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RecentTransactionsWidget()
}