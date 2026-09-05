package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** An uppercase, letter-spaced section label — matches the reference design's `.field-label`.
 * Used above every form field (amount, date, category) app-wide. */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 9.dp, start = 2.dp)
    )
}
