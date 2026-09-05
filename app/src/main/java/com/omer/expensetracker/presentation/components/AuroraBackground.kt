package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.AccentCyan
import com.omer.expensetracker.ui.theme.AccentTeal
import com.omer.expensetracker.ui.theme.BackgroundBase

/** Soft ambient glow blobs behind screen content — a radial-gradient stand-in for the
 * reference design's blurred aurora orbs (real blur is API 31+ only, so a gradient falloff
 * is used instead to look identical on every supported device). */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBase)
    ) {
        Glow(color = AccentBlueDeep, size = 340.dp, x = (-110).dp, y = (-110).dp, alpha = 0.40f)
        Glow(color = AccentCyan, size = 320.dp, x = 240.dp, y = 210.dp, alpha = 0.26f)
        Glow(color = AccentTeal, size = 280.dp, x = (-110).dp, y = 560.dp, alpha = 0.20f)
    }
}

@Composable
private fun Glow(color: Color, size: androidx.compose.ui.unit.Dp, x: androidx.compose.ui.unit.Dp, y: androidx.compose.ui.unit.Dp, alpha: Float) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .background(
                Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
                ),
                RectangleShape
            )
    )
}
