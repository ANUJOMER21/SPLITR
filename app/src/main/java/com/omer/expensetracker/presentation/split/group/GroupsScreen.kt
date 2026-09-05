package com.omer.expensetracker.presentation.split.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.GradientIconBadge
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.util.CategoryIconProvider
import com.omer.expensetracker.ui.theme.BadgeGradients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onBack: () -> Unit,
    onAddGroup: () -> Unit,
    onOpenGroup: (String) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Groups", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGroup) { Icon(Icons.Filled.Add, contentDescription = "Add group") }
        }
    ) { padding ->
        if (state.groups.isEmpty() && !state.isLoading) {
            EmptyState(
                icon = Icons.Filled.Groups,
                title = "No groups yet",
                subtitle = "Create a group for a trip, a household, or anything you split repeatedly",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(vertical = 10.dp)) {
                items(state.groups, key = { it.id }) { group ->
                    ListItemCard(onClick = { onOpenGroup(group.id) }, modifier = Modifier.animateItem()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            GradientIconBadge(icon = CategoryIconProvider.iconFor(group.iconKey), gradient = BadgeGradients[group.id.hashCode().mod(BadgeGradients.size)])
                            Text(group.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}
