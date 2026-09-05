package com.omer.expensetracker.widget

import android.annotation.SuppressLint
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
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer

import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.omer.expensetracker.R

/**
 * 3x1 Quick Add widget
 * - Fully transparent background
 * - Two fixed-height, flexible-width gradient pill buttons
 * - Bold large text + clear icons
 */
class QuickAddWidget : GlanceAppWidget() {

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color.Transparent)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickAddButton(
                    label = "Expense",
                    icon = R.drawable.ic_widget_minus,
                    backgroundRes = R.drawable.bg_quick_add_expense,
                    type = "EXPENSE",
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(12.dp))

                QuickAddButton(
                    label = "Income",
                    icon = R.drawable.ic_widget_plus,
                    backgroundRes = R.drawable.bg_quick_add_income,
                    type = "INCOME",
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun QuickAddButton(
    label: String,
    icon: Int,
    backgroundRes: Int,
    type: String,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .height(54.dp)                          // better height
            .background(ImageProvider(backgroundRes))
            .cornerRadius(999.dp)                   // extra safety for rounding
            .clickable(
                actionStartActivity(
                    Intent(context, QuickAddActivity::class.java),
                    parameters = actionParametersOf(QuickAddActivity.EXTRA_TYPE to type)
                )
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = null,
                modifier = GlanceModifier.size(24.dp)   // larger, clearer icon
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 18.sp,                 // bigger & bold
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}