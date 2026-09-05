package com.omer.expensetracker.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.omer.expensetracker.presentation.util.formatAsCurrency

/** A currency amount that counts up (or down) from its previous value instead of snapping —
 * draws the eye to what changed (month switch, new entry, contribution logged) rather than
 * just replacing static text. */
@Composable
fun AnimatedCurrencyText(
    amountMinor: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current
) {
    val animatable = remember { Animatable(amountMinor.toFloat()) }
    LaunchedEffect(amountMinor) {
        animatable.animateTo(
            targetValue = amountMinor.toFloat(),
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }
    Text(
        text = animatable.value.toLong().formatAsCurrency(),
        style = style,
        color = color,
        modifier = modifier
    )
}
