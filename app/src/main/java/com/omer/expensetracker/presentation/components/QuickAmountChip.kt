package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass

/** A quick-add amount pill — matches the reference design's `.chip.amt`. Tapping adds the
 * shown amount on top of whatever is already typed in the amount field. */
@Composable
fun QuickAmountChip(amount: Int, onClick: () -> Unit) {
    Text(
        "+${(amount * 100L).formatAsCurrency()}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(SurfaceGlass, MaterialTheme.shapes.small)
            .border(1.dp, BorderGlass, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}
