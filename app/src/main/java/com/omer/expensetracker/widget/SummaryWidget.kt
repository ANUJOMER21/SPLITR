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
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.omer.expensetracker.MainActivity
import com.omer.expensetracker.presentation.util.formatAsCurrency
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class SummaryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val summary = context.widgetEntryPoint().getMonthlySummaryUseCase()(YearMonth.now()).first()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.Background)
                    .cornerRadius(20.dp)
                    .padding(16.dp)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            ) {
                Text(
                    "THIS MONTH",
                    style = TextStyle(color = WidgetColors.OnSurfaceMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                )
                Text(
                    summary.netMinor.formatAsCurrency(),
                    style = TextStyle(
                        color = if (summary.netMinor >= 0) WidgetColors.Income else WidgetColors.Expense,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(top = 4.dp, bottom = 8.dp)
                )
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text("Expense", style = TextStyle(color = WidgetColors.OnSurfaceMuted, fontSize = 11.sp))
                        Text(
                            summary.totalExpenseMinor.formatAsCurrency(),
                            style = TextStyle(color = WidgetColors.Expense, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text("Income", style = TextStyle(color = WidgetColors.OnSurfaceMuted, fontSize = 11.sp))
                        Text(
                            summary.totalIncomeMinor.formatAsCurrency(),
                            style = TextStyle(color = WidgetColors.Income, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

class SummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SummaryWidget()
}
