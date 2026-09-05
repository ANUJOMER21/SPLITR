package com.omer.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class QuickAddWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.Background)
                    .cornerRadius(20.dp)
                    .padding(12.dp)
            ) {
                Text(
                    "Quick add",
                    style = TextStyle(color = WidgetColors.OnSurfaceMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    QuickAddButton(
                        label = "+ Expense",
                        background = WidgetColors.Primary,
                        textColor = WidgetColors.OnPrimary,
                        type = "EXPENSE",
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    QuickAddButton(
                        label = "+ Income",
                        background = WidgetColors.Surface,
                        textColor = WidgetColors.OnSurface,
                        type = "INCOME",
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAddButton(
    label: String,
    background: ColorProvider,
    textColor: ColorProvider,
    type: String,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .background(background)
            .cornerRadius(14.dp)
            .padding(vertical = 12.dp)
            .clickable(
                actionStartActivity(
                    Intent(context, QuickAddActivity::class.java),
                    parameters = actionParametersOf(QuickAddActivity.EXTRA_TYPE to type)
                )
            ),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            label,
            style = TextStyle(color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        )
    }
}

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}
