package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.ui.theme.Shapes

/** A rounded-square icon chip filled with a two-stop gradient — used for the More-menu rows
 * and anywhere a category/feature icon needs to read as a distinct, colorful glyph rather
 * than a flat tonal circle. */
@Composable
fun GradientIconBadge(
    icon: ImageVector,
    gradient: Pair<Color, Color>,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(listOf(gradient.first, gradient.second)), Shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}
