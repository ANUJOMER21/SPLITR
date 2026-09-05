package com.omer.expensetracker.presentation.split.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.AccentCoral
import com.omer.expensetracker.ui.theme.AccentTeal

/** [netMinor] positive means the friend owes the app owner; negative means the owner owes them. */
fun netMinorLabel(netMinor: Long): String = when {
    netMinor > 0 -> "owes you ${netMinor.formatAsCurrency()}"
    netMinor < 0 -> "you owe ${(-netMinor).formatAsCurrency()}"
    else -> "settled up"
}

@Composable
@ReadOnlyComposable
fun netMinorColor(netMinor: Long): Color = when {
    netMinor > 0 -> AccentTeal
    netMinor < 0 -> AccentCoral
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
