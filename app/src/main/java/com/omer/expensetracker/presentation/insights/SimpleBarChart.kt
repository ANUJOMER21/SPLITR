package com.omer.expensetracker.presentation.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class BarDatum(val label: String, val value: Float)

/** A minimal hand-rolled bar chart — no external charting dependency, consistent with the
 * donut chart used elsewhere in the app. Bars scale to the tallest value in the set. */
@Composable
fun SimpleBarChart(data: List<BarDatum>, barColor: Color, modifier: Modifier = Modifier) {
    val maxValue = data.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 4.dp)
        ) {
            if (data.isEmpty()) return@Canvas
            val barWidth = size.width / (data.size * 1.6f)
            val gap = barWidth * 0.6f
            data.forEachIndexed { index, datum ->
                val barHeight = (datum.value / maxValue) * size.height
                val x = index * (barWidth + gap)
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
            data.forEach { datum ->
                Text(
                    datum.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
