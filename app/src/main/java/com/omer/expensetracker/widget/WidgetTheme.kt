package com.omer.expensetracker.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * Shared visual tokens for widgets that sit on the dark gradient background
 * (`res/drawable/widget_gradient_background.xml`). Kept separate from [WidgetColors] since
 * these are translucent overlays tuned specifically for a dark backdrop, not general-purpose
 * light/dark-aware surface colors — don't reuse them anywhere with a light background.
 */
object WidgetTheme {
    val ChipBackground = ColorProvider(Color.White.copy(alpha = 0.12f))
    val CardBackground = ColorProvider(Color.White.copy(alpha = 0.08f))
    val RowBackground = ColorProvider(Color.White.copy(alpha = 0.06f))
    val TextPrimary = ColorProvider(Color.White)
    val TextSecondary = ColorProvider(Color.White.copy(alpha = 0.65f))
    val TextTertiary = ColorProvider(Color.White.copy(alpha = 0.45f))
}

/** Centered icon-chip + title + subtitle, used by both widgets for their "nothing to show
 * yet" states (no friends, all settled up, no transactions). */
@Composable
fun EmptyState(icon: String, title: String, subtitle: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier.size(40.dp).background(WidgetTheme.ChipBackground).cornerRadius(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, style = TextStyle(color = WidgetTheme.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold))
        }
        Text(
            title,
            style = TextStyle(color = WidgetTheme.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
            modifier = GlanceModifier.padding(top = 8.dp)
        )
        Text(
            subtitle,
            style = TextStyle(color = WidgetTheme.TextTertiary, fontSize = 12.sp),
            maxLines = 1,
            modifier = GlanceModifier.padding(top = 2.dp)
        )
    }
}