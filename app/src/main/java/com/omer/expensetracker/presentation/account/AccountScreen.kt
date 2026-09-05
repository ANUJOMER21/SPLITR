package com.omer.expensetracker.presentation.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.sync.SyncStatus
import com.omer.expensetracker.presentation.components.GlassButton
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.presentation.components.ListItemCard
import com.omer.expensetracker.presentation.components.TonalIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onSignIn: () -> Unit,
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.syncState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Account & sync", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { TonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            ListItemCard(onClick = {}, modifier = Modifier.padding(top = 12.dp)) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(
                        when (state.status) {
                            SyncStatus.SIGNED_OUT -> "Not signed in"
                            SyncStatus.SIGNED_IN -> "Signed in as ${state.user?.email ?: state.user?.displayName ?: "unknown"}"
                            SyncStatus.SYNCING -> "Syncing…"
                            SyncStatus.ERROR -> "Sync error"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        if (state.pendingUploads > 0) "${state.pendingUploads} change(s) waiting to sync"
                        else "Everything is backed up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (state.status == SyncStatus.SIGNED_OUT) {
                GradientButton(
                    text = "Sign in",
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                )
            } else {
                GlassButton(text = "Sign out", onClick = viewModel::signOut, modifier = Modifier.padding(top = 20.dp))
            }
        }
    }
}
