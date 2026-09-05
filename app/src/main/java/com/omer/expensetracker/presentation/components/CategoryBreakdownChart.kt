package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.domain.model.CategoryBreakdownItem
import com.omer.expensetracker.presentation.util.formatAsCurrency
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun CategoryBreakdownChart(
    items: List<CategoryBreakdownItem>,
    totalMinor: Long,
    onSliceClick: (categoryId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokeWidthDp = 12.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(items) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = tapOffset.x - center.x
                        val dy = tapOffset.y - center.y
                        val radius = hypot(dx, dy)
                        val outerRadius = size.width / 2f
                        val innerRadius = outerRadius - strokeWidthDp.toPx()
                        if (radius in innerRadius..outerRadius && items.isNotEmpty()) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 90.0
                            if (angle < 0) angle += 360.0
                            var accumulated = 0f
                            for (item in items) {
                                val sweep = if (totalMinor > 0) item.totalMinor * 360f / totalMinor else 0f
                                if (angle < accumulated + sweep) {
                                    onSliceClick(item.category.id)
                                    break
                                }
                                accumulated += sweep
                            }
                        }
                    }
                }
                .semantics { contentDescription = "Category breakdown chart" }
        ) {
            val strokeWidthPx = strokeWidthDp.toPx()
            val diameter = size.minDimension - strokeWidthPx
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            if (items.isEmpty()) {
                drawArc(
                    color = Color(0xFFDCDAE8),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                )
            } else {
                var startAngle = -90f
                items.forEach { item ->
                    val sweep = if (totalMinor > 0) item.totalMinor * 360f / totalMinor else 0f
                    drawArc(
                        color = Color(item.category.colorArgb),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
        }

        Box(contentAlignment = Alignment.Center) {
            Text(
                text = totalMinor.formatAsCurrency(),
                modifier= Modifier.padding(4.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
