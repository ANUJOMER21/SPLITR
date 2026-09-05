package com.omer.expensetracker.widget

import androidx.glance.unit.ColorProvider
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.AccentCoral
import com.omer.expensetracker.ui.theme.AccentCyan
import com.omer.expensetracker.ui.theme.AccentTeal
import com.omer.expensetracker.ui.theme.AccentViolet
import com.omer.expensetracker.ui.theme.BackgroundWashTop
import com.omer.expensetracker.ui.theme.SurfaceGlass2
import com.omer.expensetracker.ui.theme.TextPrimary
import com.omer.expensetracker.ui.theme.TextSecondary
import com.omer.expensetracker.ui.theme.TextTertiary

/** Glance widgets can't read MaterialTheme, and can't use Brush gradients — these reuse the
 * app's actual palette constants directly ([com.omer.expensetracker.ui.theme.Color]) so a
 * future theme change here propagates to the home-screen widgets automatically instead of
 * silently drifting out of sync again. Must use the Compose-[androidx.compose.ui.graphics.Color]
 * overload of [ColorProvider] — the Int overload is a @ColorRes *resource id* lookup, not a raw
 * ARGB value, and fails at render time. */
object WidgetColors {
    val Background = ColorProvider(BackgroundWashTop)
    val Surface = ColorProvider(SurfaceGlass2)
    val OnSurface = ColorProvider(TextPrimary)
    val OnSurfaceMuted = ColorProvider(TextSecondary)
    val OnSurfaceFaint = ColorProvider(TextTertiary)

    val Primary = ColorProvider(AccentBlue)
    val PrimaryDeep = ColorProvider(AccentBlueDeep)
    val OnPrimary = ColorProvider(androidx.compose.ui.graphics.Color.White)
    val Cyan = ColorProvider(AccentCyan)
    val Violet = ColorProvider(AccentViolet)

    val Expense = ColorProvider(AccentCoral)
    val Income = ColorProvider(AccentTeal)
}
