package com.omer.expensetracker.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.auth.AuthGateViewModel
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import kotlinx.coroutines.delay

private const val MIN_DISPLAY_MS = 500L

@Composable
fun SplashScreen(
    onSignedIn: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: AuthGateViewModel = hiltViewModel()
) {
    val isSignedIn by viewModel.isSignedIn.collectAsState()

    LaunchedEffect(isSignedIn) {
        val signedIn = isSignedIn ?: return@LaunchedEffect
        delay(MIN_DISPLAY_MS)
        if (signedIn) onSignedIn() else onSignedOut()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(Brush.linearGradient(listOf(AccentBlueDeep, AccentBlue)), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PieChart, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(44.dp))
            }
            Text(
                "Expense Tracker",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 20.dp)
            )
            CircularProgressIndicator(modifier = Modifier.padding(top = 28.dp).size(28.dp), strokeWidth = 3.dp)
        }
    }
}
