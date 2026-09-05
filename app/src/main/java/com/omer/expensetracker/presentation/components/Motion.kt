package com.omer.expensetracker.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** Subtle spring-back scale-down while pressed — the tactile cue that reads as "modern app"
 * instead of a flat, static tap target. */
@Composable
fun Modifier.pressScale(pressed: Boolean, scaleDown: Float = 0.95f): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 700f),
        label = "pressScale"
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource, scaleDown: Float = 0.95f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    return pressScale(pressed, scaleDown)
}

/** Wraps [onClick] with a short haptic buzz, fired once per tap — unlike [pressScale] this
 * reacts to the click event itself, not press state, so it can't double-fire during a held
 * press. [type] defaults to a light tick; pass [HapticFeedbackType.LongPress] for a stronger
 * buzz on destructive or long-press actions. */
@Composable
fun hapticClick(onClick: () -> Unit, type: HapticFeedbackType = HapticFeedbackType.TextHandleMove): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return {
        haptic.performHapticFeedback(type)
        onClick()
    }
}
