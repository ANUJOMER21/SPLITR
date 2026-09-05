package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.AccentViolet

/**
 * Ambient background behind screen content.
 *
 * One continuous diagonal gradient built from a single hue family — violet through deep blue
 * only, no cyan/teal/green — pulled toward black so it reads as dark rather than a bright
 * poster of color. Fully static: no shapes, streaks, or motion, just the gradient plus two
 * faint, heavily-blurred highlights for depth.
 *
 * @param darkness how far each hue is blended toward black, 0f (original vivid accent colors)
 * to 1f (fully black). Raise it for a moodier, lower-contrast background; lower it if the
 * gradient starts to lose its color identity.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    darkness: Float = 0.6f
) {
    val violetBlueBlend = lerp(AccentViolet, AccentBlueDeep, 0.5f)

    val darkViolet = lerp(AccentViolet, Color.Black, darkness)
    val darkBlend = lerp(violetBlueBlend, Color.Black, darkness)
    val darkBlue = lerp(AccentBlueDeep, Color.Black, (darkness * 0.6f).coerceIn(0f, 1f))
    val deepestBlue = lerp(AccentBlueDeep, Color.Black, (darkness * 1.1f).coerceIn(0f, 1f))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to darkViolet,
                        0.35f to darkBlend,
                        0.70f to darkBlue,
                        1.00f to deepestBlue
                    )
                )
            )
            .clipToBounds()
    ) {
        // Faint highlight, upper-left — a hint of sheen, not a light source.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-180).dp, y = (-220).dp)
                .size(760.dp)
                .blur(160.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
        )

        // Faint highlight, lower-right — same blue/violet family, no green cast.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 160.dp, y = 200.dp)
                .size(700.dp)
                .blur(160.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentBlueDeep.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
        )

        // Thin scrim for extra unity on top of the already-dark gradient.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
        )
    }
}