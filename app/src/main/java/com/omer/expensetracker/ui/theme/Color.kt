package com.omer.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

// Aurora dark palette — deep near-black base, glassy translucent surfaces, blue/cyan accent.
val BackgroundBase = Color(0xFF0D0B18)
val SurfaceGlass = Color(0x0EFFFFFF)
val SurfaceGlass2 = Color(0x1AFFFFFF)
val BorderGlass = Color(0x17FFFFFF)

val TextPrimary = Color(0xFFF5F3FC)
val TextSecondary = Color(0xFFA79FC4)
val TextTertiary = Color(0xFF6E6690)

val AccentBlue = Color(0xFF3B82F6)
val AccentBlueDeep = Color(0xFF2563EB)
val AccentCyan = Color(0xFF06B6D4)
val AccentAmber = Color(0xFFF59E0B)
val AccentCoral = Color(0xFFFB7159)
val AccentTeal = Color(0xFF2DD4BF)
val AccentViolet = Color(0xFF8B5CF6)
val AccentMagenta = Color(0xFFE84DD3)

// Background wash — the flat near-black base plus a subtle top-to-bottom tint shift,
// so the app background itself reads as a gradient rather than one flat color.
val BackgroundWashTop = Color(0xFF160F2E)
val BackgroundWashBottom = Color(0xFF07050F)

// Semantic aliases used across screens — coral reads as the "expense/negative" accent,
// teal as the "income/positive" one, matching the reference design's category coloring.
val AccentPrimary = AccentBlue
val OnAccentPrimary = Color(0xFFFFFFFF)
val AccentPrimaryContainer = Color(0x243B82F6)
val OnAccentPrimaryContainer = AccentBlue

val ExpenseRed = AccentCoral
val OnExpenseRed = Color(0xFFFFFFFF)
val ExpenseRedContainer = Color(0x24FB7159)

val IncomeGreen = AccentTeal
val OnIncomeGreen = Color(0xFF04211D)
val IncomeGreenContainer = Color(0x242DD4BF)

val WarningAmber = AccentAmber

val HeroGradientStart = AccentBlueDeep
val HeroGradientEnd = AccentCyan
val OnHeroGradientPrimary = Color(0xFFFFFFFF)
val OnHeroGradientMuted = Color(0xBFFFFFFF)

// Gradient badge pairs for icon chips (categories, More rows) — cycled by index so
// each entry reads as visually distinct rather than one flat tint repeated everywhere.
val BadgeGradients = listOf(
    AccentBlueDeep to AccentBlue,
    Color(0xFF22D3EE) to AccentCyan,
    Color(0xFFFBBF24) to AccentAmber,
    Color(0xFFFF9166) to AccentCoral,
    Color(0xFF4EE0CB) to AccentTeal
)
