package com.omer.expensetracker.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass

/** Blue-accented segmented-button colors — the default M3 gray selection reads as an
 * unthemed leftover against the rest of the app's blue/glass system. Used everywhere a
 * [androidx.compose.material3.SegmentedButton] appears (Insights period, recurring
 * type/unit, reminder repeat cadence, entry-list type filter). */
@Composable
fun themedSegmentedColors(): SegmentedButtonColors = SegmentedButtonDefaults.colors(
    activeContainerColor = AccentBlue,
    activeContentColor = androidx.compose.ui.graphics.Color.White,
    activeBorderColor = AccentBlue,
    inactiveContainerColor = SurfaceGlass,
    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    inactiveBorderColor = BorderGlass
)
