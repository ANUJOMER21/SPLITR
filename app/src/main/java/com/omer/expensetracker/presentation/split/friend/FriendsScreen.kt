package com.omer.expensetracker.presentation.split.friend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PeopleAlt
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
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.split.components.FriendAvatar
import com.omer.expensetracker.presentation.split.components.netMinorColor
import com.omer.expensetracker.presentation.split.components.netMinorLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    onAddFriend: () -> Unit,
    onOpenFriend: (String) -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Friends", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddFriend) { Icon(Icons.Filled.Add, contentDescription = "Add friend") }
        }
    ) { padding ->
        if (state.friends.isEmpty() && !state.isLoading) {
            EmptyState(
                icon = Icons.Filled.PeopleAlt,
                title = "No friends yet",
                subtitle = "Add a friend to start tracking shared expenses with them",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(state.friends, key = { it.friend.id }) { item ->
                    ListItemCard(onClick = { onOpenFriend(item.friend.id) }, modifier = Modifier.animateItem()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            FriendAvatar(item.friend.name, item.friend.avatarColorArgb)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.friend.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    netMinorLabel(item.netMinor),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = netMinorColor(item.netMinor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
