package com.omer.expensetracker.presentation.split.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.categories.CATEGORY_COLOR_PALETTE
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.FieldLabel
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.presentation.components.GradientIconBadge
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.split.components.FriendAvatar
import com.omer.expensetracker.presentation.util.CategoryIconProvider
import com.omer.expensetracker.ui.theme.BadgeGradients
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditGroupScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    onAddFriendInline: () -> Unit,
    viewModel: AddEditGroupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) { if (state.isSaved) onDone() }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New group", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            Box(modifier = Modifier.padding(bottom = 20.dp)) {
                GradientIconBadge(icon = CategoryIconProvider.iconFor(state.iconKey), gradient = BadgeGradients[0], size = 64.dp, iconSize = 30.dp)
            }

            FieldLabel("Name")
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                isError = state.errorMessage != null,
                supportingText = { state.errorMessage?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            FieldLabel("Icon", modifier = Modifier.padding(top = 20.dp))
            FlowRow {
                CategoryIconProvider.pickableKeys.forEach { key ->
                    val selected = key == state.iconKey
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                            .clickable { viewModel.onIconSelect(key) }
                            .background(SurfaceGlass, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(CategoryIconProvider.iconFor(key), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            FieldLabel("Colour", modifier = Modifier.padding(top = 20.dp))
            FlowRow {
                CATEGORY_COLOR_PALETTE.forEach { color ->
                    val selected = color == state.colorArgb
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                            .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                            .clickable { viewModel.onColorSelect(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 26.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Members", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Add friend",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onAddFriendInline() }
                )
            }

            if (state.allFriends.isEmpty()) {
                EmptyState(title = "No friends yet", subtitle = "Add a friend first, then bring them into this group")
            } else {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    state.allFriends.forEach { friend ->
                        val selected = friend.id in state.selectedFriendIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { viewModel.toggleFriend(friend.id) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            FriendAvatar(friend.name, friend.avatarColorArgb, size = 40.dp)
                            Text(friend.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) MaterialTheme.colorScheme.primary else SurfaceGlass)
                                    .border(1.dp, BorderGlass, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            GradientButton(text = "Create group", onClick = viewModel::save, modifier = Modifier.padding(top = 28.dp, bottom = 20.dp))
        }
    }
}
