package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass

/** A glass icon button — translucent surface with a hairline border, matching the reference
 * design's `.icon-btn`. Springs down slightly on press for tactile feedback. */
@Composable
fun TonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .size(40.dp)
            .pressScale(interactionSource, scaleDown = 0.88f)
            .clip(CircleShape)
            .background(SurfaceGlass)
            .border(1.dp, BorderGlass, CircleShape)
    ) {
        content()
    }
}
