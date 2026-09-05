package com.omer.expensetracker.presentation.split

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.omer.expensetracker.presentation.split.friend.FriendsScreen
import com.omer.expensetracker.presentation.split.group.GroupsScreen

private enum class SplitTab { FRIENDS, GROUPS }

/** Top-level bottom-nav destination for Split with Friends — a Friends / Groups switcher that
 * embeds the existing list screens (which drop their own top bar when [embedded]). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitHomeScreen(
    onOpenFriend: (String) -> Unit,
    onAddFriend: () -> Unit,
    onOpenGroup: (String) -> Unit,
    onAddGroup: () -> Unit
) {
    var tab by remember { mutableStateOf(SplitTab.FRIENDS) }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Split", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab.ordinal, containerColor = Color.Transparent) {
                SplitTab.entries.forEach { t ->
                    Tab(
                        selected = tab == t,
                        onClick = { tab = t },
                        text = { Text(if (t == SplitTab.FRIENDS) "Friends" else "Groups") }
                    )
                }
            }
            when (tab) {
                SplitTab.FRIENDS -> FriendsScreen(
                    onBack = {},
                    onAddFriend = onAddFriend,
                    onOpenFriend = onOpenFriend,
                    embedded = true
                )
                SplitTab.GROUPS -> GroupsScreen(
                    onBack = {},
                    onAddGroup = onAddGroup,
                    onOpenGroup = onOpenGroup,
                    embedded = true
                )
            }
        }
    }
}
