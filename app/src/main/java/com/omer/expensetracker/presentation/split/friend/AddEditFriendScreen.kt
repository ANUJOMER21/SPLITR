package com.omer.expensetracker.presentation.split.friend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.categories.CATEGORY_COLOR_PALETTE
import com.omer.expensetracker.presentation.components.FieldLabel
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.presentation.components.GlassButton
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.split.components.FriendAvatar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditFriendScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditFriendViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) { if (state.isSaved && state.notRegisteredNotice == null) onDone() }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New friend", style = MaterialTheme.typography.titleLarge) },
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
                FriendAvatar(state.name.ifBlank { "?" }, state.colorArgb, size = 72.dp)
            }

            FieldLabel("Name")
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            FieldLabel("Email", modifier = Modifier.padding(top = 20.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                supportingText = { Text("Required — this is how we find their account and sync with them") },
                isError = state.errorMessage != null,
                modifier = Modifier.fillMaxWidth()
            )
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }

            FieldLabel("Contact (optional)", modifier = Modifier.padding(top = 20.dp))
            OutlinedTextField(
                value = state.contactInfo,
                onValueChange = viewModel::onContactInfoChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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

            if (state.isSaved && state.notRegisteredNotice != null) {
                Text(
                    "${state.notRegisteredNotice} — added, and their balance will track normally, but nothing syncs to them until they sign up with this email.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
                GlassButton(text = "Got it", onClick = onDone, modifier = Modifier.padding(top = 14.dp))
            } else {
                GradientButton(text = "Add friend", onClick = viewModel::save, modifier = Modifier.padding(top = 28.dp))
            }
        }
    }
}
