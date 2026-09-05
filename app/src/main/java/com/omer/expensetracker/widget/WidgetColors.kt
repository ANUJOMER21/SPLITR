package com.omer.expensetracker.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

/** Glance widgets can't read MaterialTheme — these mirror the app's dark palette
 * ([com.omer.expensetracker.ui.theme.Color]) so the widget matches the app exactly.
 * Must use the Compose-[Color] overload of [ColorProvider] — the Int overload is a
 * @ColorRes *resource id* lookup, not a raw ARGB value, and fails at render time. */
object WidgetColors {
    val Background = ColorProvider(Color(0xFF15161C))
    val Surface = ColorProvider(Color(0xFF1E2028))
    val OnSurface = ColorProvider(Color(0xFFECEDF2))
    val OnSurfaceMuted = ColorProvider(Color(0xFFA3A6B4))
    val Primary = ColorProvider(Color(0xFF8C7CFF))
    val OnPrimary = ColorProvider(Color(0xFF1B1030))
    val Expense = ColorProvider(Color(0xFFFF6B7F))
    val Income = ColorProvider(Color(0xFF35D48C))
}
