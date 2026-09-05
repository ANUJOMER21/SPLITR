package com.omer.expensetracker.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.omer.expensetracker.ui.theme.AccentBlue

private data class BottomTab(val route: String, val navigateRoute: String, val label: String, val icon: ImageVector)

/** [route] is the destination's route pattern (used to detect the selected tab against
 * [NavDestination.hierarchy]); [navigateRoute] is the concrete route passed to
 * [androidx.navigation.NavController.navigate] — they differ for EntryList, whose route
 * pattern carries an optional query placeholder that isn't itself navigable. */
private val bottomTabs = listOf(
    BottomTab(Screen.Dashboard.route, Screen.Dashboard.route, "Home", Icons.Filled.Home),
    BottomTab(Screen.EntryList.route, Screen.EntryList.route(), "Entries", Icons.AutoMirrored.Filled.List),
    BottomTab(Screen.Insights.route, Screen.Insights.route, "Insights", Icons.Filled.BarChart),
    BottomTab(Screen.More.route, Screen.More.route, "More", Icons.Filled.MoreHoriz)
)

/** True for routes that own a bottom-nav tab — the bar only shows on these top-level
 * destinations, staying hidden on every pushed detail/edit screen. */
fun NavDestination?.isTopLevelDestination(): Boolean =
    this != null && bottomTabs.any { tab -> hierarchy.any { it.route == tab.route } }

/** A floating glass pill nav bar — matches the reference design's `.bottom-nav`: a translucent
 * dark strip with a soft blue pill indicator behind the active icon+label. */
@Composable
fun ExpenseTrackerBottomNavBar(currentDestination: NavDestination?, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp, top = 2.dp)
            .background(Color(0xF0120E20), MaterialTheme.shapes.extraLarge)
            .padding(9.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        bottomTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
            val contentColor = if (selected) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(
                        if (selected) AccentBlue.copy(alpha = 0.14f) else Color.Transparent,
                        MaterialTheme.shapes.medium
                    )
                    .clickable {
                        navController.navigate(tab.navigateRoute) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Icon(tab.icon, contentDescription = tab.label, tint = contentColor, modifier = Modifier)
                Text(
                    tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
