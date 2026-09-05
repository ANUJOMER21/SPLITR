package com.omer.expensetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Aurora dark theme by design: deep near-black base with glassy translucent surfaces and a
// blue/cyan accent — the app's single deliberate look, not tied to the system theme.
private val AppColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = OnAccentPrimary,
    primaryContainer = AccentPrimaryContainer,
    onPrimaryContainer = OnAccentPrimaryContainer,
    secondary = AccentCyan,
    onSecondary = Color.White,
    tertiary = IncomeGreen,
    onTertiary = OnIncomeGreen,
    background = BackgroundBase,
    onBackground = TextPrimary,
    surface = BackgroundBase,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGlass2,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceGlass,
    surfaceContainerHigh = SurfaceGlass2,
    surfaceContainerLow = BackgroundBase,
    outline = BorderGlass,
    outlineVariant = BorderGlass,
    error = ExpenseRed,
    onError = OnExpenseRed,
    errorContainer = ExpenseRedContainer,
    onErrorContainer = ExpenseRed
)

@Composable
fun ExpenseTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
