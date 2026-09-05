package com.omer.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
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
import com.omer.expensetracker.presentation.util.formatAsCurrency
import kotlinx.coroutines.flow.first
import java.time.YearMonth

/** This month's expense/income/net, sharing the same dark gradient identity and glass-chip
 * styling (see [WidgetTheme]) as [BalanceWidget] and [RecentTransactionsWidget]. */
class SummaryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val summary = context.widgetEntryPoint().getMonthlySummaryUseCase()(YearMonth.now()).first()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.widget_gradient_background))
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                    .padding(18.dp)
            ) {
                SummaryHeader(onRefresh = actionRunCallback<RefreshSummaryAction>())
                Spacer(modifier = GlanceModifier.height(12.dp))

                Text(
                    text = "OVERALL, THIS MONTH",
                    style = TextStyle(color = WidgetTheme.TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
                Text(
                    text = summary.netMinor.formatAsCurrency(),
                    style = TextStyle(
                        color = if (summary.netMinor >= 0) WidgetColors.Income else WidgetColors.Expense,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.padding(top = 2.dp, bottom = 14.dp)
                )

                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    SummaryStat(
                        label = "Expense",
                        amountMinor = summary.totalExpenseMinor,
                        symbol = "−",
                        color = WidgetColors.Expense,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(10.dp))
                    SummaryStat(
                        label = "Income",
                        amountMinor = summary.totalIncomeMinor,
                        symbol = "+",
                        color = WidgetColors.Income,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryHeader(onRefresh: Action) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = GlanceModifier.size(30.dp), contentAlignment = Alignment.Center) {
            Image(
                provider = ImageProvider(R.drawable.logo),
                contentDescription = null,
                modifier = GlanceModifier.size(30.dp).cornerRadius(5.dp)
            )
        }
        Spacer(modifier = GlanceModifier.width(10.dp))
        Text(
            "THIS MONTH",
            style = TextStyle(color = WidgetTheme.TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.width(10.dp))
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

@Composable
private fun SummaryStat(
    label: String,
    amountMinor: Long,
    symbol: String,
    color: ColorProvider,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(WidgetTheme.CardBackground)
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier.size(20.dp).background(WidgetTheme.ChipBackground).cornerRadius(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, style = TextStyle(color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                label,
                style = TextStyle(color = WidgetTheme.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium),
                maxLines = 1
            )
        }
        Text(
            amountMinor.formatAsCurrency(),
            style = TextStyle(color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
            modifier = GlanceModifier.padding(top = 6.dp)
        )
    }
}

/** Manually refreshes the widget's data without waiting for the system's update cycle. */
class RefreshSummaryAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        SummaryWidget().update(context, glanceId)
    }
}

class SummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SummaryWidget()
}
