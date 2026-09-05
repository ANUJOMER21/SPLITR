package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass

/** The app's primary call-to-action — a blue gradient pill, matching the reference design's
 * `.btn-primary` / `.btn-expense`. Used for every screen's main "Save" action. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: List<Color> = listOf(AccentBlueDeep, AccentBlue)
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        onClick = hapticClick(onClick),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .pressScale(interactionSource)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradient), MaterialTheme.shapes.large),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontSize = 15.5.sp, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** A secondary glass pill button — matches the reference design's `.btn-income`: a plain
 * translucent surface with a hairline border, no gradient fill. */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        onClick = hapticClick(onClick),
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        color = SurfaceGlass,
        border = BorderStroke(1.dp, BorderGlass),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .pressScale(interactionSource)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.height(17.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.width(7.dp))
            }
            Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.5.sp, style = MaterialTheme.typography.labelLarge)
        }
    }
}
