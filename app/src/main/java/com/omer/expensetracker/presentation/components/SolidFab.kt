package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.ui.theme.AccentBlue

/** The app's floating add button — a flat solid accent-color circle, not a gradient, so every
 * FAB across the app (Entries, Friends, Groups, …) reads the same instead of each screen
 * re-implementing its own gradient variant. */
@Composable
fun SolidFab(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(60.dp)
            .pressScale(interactionSource)
            .background(AccentBlue, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = hapticClick(onClick),
            interactionSource = interactionSource,
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Filled.Add, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.height(28.dp))
            }
        }
    }
}
