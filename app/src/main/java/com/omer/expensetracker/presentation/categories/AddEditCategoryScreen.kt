package com.omer.expensetracker.presentation.categories

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.presentation.components.CategoryDot
import com.omer.expensetracker.presentation.components.pressScale
import com.omer.expensetracker.presentation.util.CategoryIconProvider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditCategoryScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditCategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditing) "Edit category" else "New category") },
                navigationIcon = {
                    com.omer.expensetracker.presentation.components.TonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CategoryDot(state.colorArgb, state.iconKey, size = 64.dp, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Category name") },
                isError = state.errorMessage != null,
                supportingText = { state.errorMessage?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Icon", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            FlowRow(modifier = Modifier.padding(bottom = 16.dp)) {
                CategoryIconProvider.pickableKeys.forEach { key ->
                    val selected = key == state.iconKey
                    val borderColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        label = "iconBorderColor"
                    )
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, borderColor, CircleShape)
                            .clickable { viewModel.onIconSelect(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryDot(
                            colorArgb = if (selected) state.colorArgb else 0xFFBDBDBD,
                            iconKey = key,
                            size = 40.dp,
                            selected = selected
                        )
                    }
                }
            }

            Text("Colour", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow {
                CATEGORY_COLOR_PALETTE.forEach { color ->
                    val selected = color == state.colorArgb
                    val scale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (selected) 1.1f else 1f,
                        label = "colorScale"
                    )
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(40.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(CircleShape)
                            .background(Color(color))
                            .then(
                                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier
                            )
                            .clickable { viewModel.onColorSelect(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            com.omer.expensetracker.presentation.components.GradientButton(
                text = "Save",
                onClick = viewModel::save,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}
