package com.omer.expensetracker.presentation.split.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A colour + initial avatar — every friend has one instead of a photo requirement. */
@Composable
fun FriendAvatar(name: String, colorArgb: Long, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val bg = Color(colorArgb)
    Box(
        modifier = modifier.size(size).background(bg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.trim().take(1).uppercase().ifBlank { "?" },
            style = MaterialTheme.typography.titleMedium,
            color = if (bg.luminance() > 0.5f) Color.Black else Color.White
        )
    }
}
