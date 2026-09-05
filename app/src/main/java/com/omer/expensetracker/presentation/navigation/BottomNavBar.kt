package com.omer.expensetracker.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.omer.expensetracker.presentation.components.hapticClick
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.AccentCyan
import com.omer.expensetracker.ui.theme.TextPrimary

private data class BottomTab(val route: String, val navigateRoute: String, val label: String, val icon: ImageVector)

/** [route] is the destination's route pattern (used to detect the selected tab against
 * [NavDestination.hierarchy]); [navigateRoute] is the concrete route passed to
 * [androidx.navigation.NavController.navigate] — they differ for EntryList, whose route
 * pattern carries an optional query placeholder that isn't itself navigable. */
private val bottomTabs = buildList {
    add(BottomTab(Screen.Dashboard.route, Screen.Dashboard.route, "Home", Icons.Filled.Home))
    add(BottomTab(Screen.EntryList.route, Screen.EntryList.route(), "Entries", Icons.AutoMirrored.Filled.List))
    if (com.omer.expensetracker.FeatureFlags.SPLIT_WITH_FRIENDS_ENABLED) {
        add(BottomTab(Screen.SplitHome.route, Screen.SplitHome.route, "Split", Icons.Filled.Groups))
    }
    add(BottomTab(Screen.Insights.route, Screen.Insights.route, "Insights", Icons.Filled.BarChart))
    add(BottomTab(Screen.More.route, Screen.More.route, "More", Icons.Filled.MoreHoriz))
}

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
            .padding(bottom = 14.dp, top = 0.dp)
            .background(Color(0xF0120E20), CircleShape)
            .padding(vertical = 4.dp, horizontal = 9.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        bottomTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true

            val pillSize by animateDpAsState(
                targetValue = if (selected) 36.dp else 30.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "navPillSize"
            )
            val pillAlpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = tween(220),
                label = "navPillAlpha"
            )
            val glowElevation by animateDpAsState(
                targetValue = if (selected) 14.dp else 0.dp,
                animationSpec = tween(220),
                label = "navGlowElevation"
            )
            val lift by animateDpAsState(
                targetValue = if (selected) (-3).dp else 0.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "navLift"
            )
            val iconSize by animateDpAsState(
                targetValue = if (selected) 21.dp else 19.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "navIconSize"
            )
            val iconColor by animateColorAsState(
                targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(220),
                label = "navIconColor"
            )
            val labelColor by animateColorAsState(
                targetValue = if (selected) TextPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(220),
                label = "navLabelColor"
            )

            val onTap = hapticClick({
                navController.navigate(tab.navigateRoute) {
                    popUpTo(Screen.Dashboard.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer { translationY = lift.toPx() }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTap
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier.size(pillSize),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(elevation = glowElevation, shape = CircleShape, ambientColor = AccentBlue, spotColor = AccentBlue)
                            .graphicsLayer { alpha = pillAlpha }
                            .background(Brush.linearGradient(listOf(AccentBlueDeep, AccentBlue, AccentCyan)), CircleShape)
                    )
                    Icon(tab.icon, contentDescription = tab.label, tint = iconColor, modifier = Modifier.size(iconSize))
                }
                Text(
                    tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = labelColor,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}
