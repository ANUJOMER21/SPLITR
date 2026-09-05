package com.omer.expensetracker.presentation.split.group

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.GradientIconBadge
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.components.pressScale
import com.omer.expensetracker.presentation.util.CategoryIconProvider
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.BadgeGradients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onBack: () -> Unit,
    onAddGroup: () -> Unit,
    onOpenGroup: (String) -> Unit,
    /** True when hosted inside the Split hub tab — the hub already owns the top bar. */
    embedded: Boolean = false,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            if (!embedded) {
                CenterAlignedTopAppBar(
                    title = { Text("Groups", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        floatingActionButton = { com.omer.expensetracker.presentation.components.SolidFab(onClick = onAddGroup, contentDescription = "Add group") }
    ) { padding ->
        if (state.groups.isEmpty() && !state.isLoading) {
            EmptyState(
                icon = Icons.Filled.Groups,
                title = "No groups yet",
                subtitle = "Create a group for a trip, a household, or anything you split repeatedly",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                items(state.groups, key = { it.group.id }) { item ->
                    ListItemCard(onClick = { onOpenGroup(item.group.id) }, modifier = Modifier.animateItem(), paddingValues = PaddingValues(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            GradientIconBadge(
                                icon = CategoryIconProvider.iconFor(item.group.iconKey),
                                gradient = BadgeGradients[item.group.id.hashCode().mod(BadgeGradients.size)],
                                size = 48.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.group.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${item.memberCount} ${if (item.memberCount == 1) "member" else "members"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

