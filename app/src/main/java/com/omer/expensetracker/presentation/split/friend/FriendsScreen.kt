package com.omer.expensetracker.presentation.split.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PeopleAlt
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
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.components.pressScale
import com.omer.expensetracker.presentation.split.components.FriendAvatar
import com.omer.expensetracker.presentation.split.components.netMinorColor
import com.omer.expensetracker.presentation.split.components.netMinorLabel
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.HeroGradientEnd
import com.omer.expensetracker.ui.theme.HeroGradientStart
import com.omer.expensetracker.ui.theme.OnHeroGradientMuted
import com.omer.expensetracker.ui.theme.OnHeroGradientPrimary

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
        floatingActionButton = { com.omer.expensetracker.presentation.components.SolidFab(onClick = onAddFriend, contentDescription = "Add friend") }
    ) { padding ->
        if (state.friends.isEmpty() && !state.isLoading) {
            EmptyState(
                icon = Icons.Filled.PeopleAlt,
                title = "No friends yet",
                subtitle = "Add a friend to start tracking shared expenses with them",
                modifier = Modifier.padding(padding)
            )
        } else {
            val owedToYou = state.friends.sumOf { if (it.netMinor > 0) it.netMinor else 0L }
            val youOwe = state.friends.sumOf { if (it.netMinor < 0) -it.netMinor else 0L }

            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                item { BalanceSummaryHero(owedToYou, youOwe, modifier = Modifier.padding(bottom = 16.dp)) }

                items(state.friends, key = { it.friend.id }) { item ->
                    ListItemCard(onClick = { onOpenFriend(item.friend.id) }, modifier = Modifier.animateItem(), paddingValues = PaddingValues(vertical = 6.dp)) {
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
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceSummaryHero(owedToYouMinor: Long, youOweMinor: Long, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("OVERALL", style = MaterialTheme.typography.labelSmall, color = OnHeroGradientMuted)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("You'll get", style = MaterialTheme.typography.bodyMedium, color = OnHeroGradientMuted)
                    Text(
                        owedToYouMinor.formatAsCurrency(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnHeroGradientPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("You owe", style = MaterialTheme.typography.bodyMedium, color = OnHeroGradientMuted)
                    Text(
                        youOweMinor.formatAsCurrency(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnHeroGradientPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

