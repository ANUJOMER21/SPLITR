package com.omer.expensetracker.presentation.addedit

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.presentation.components.AmountInputField
import com.omer.expensetracker.presentation.components.CategoryPickerGrid
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.FieldLabel
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.presentation.components.QuickAmountChip
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.ui.theme.AccentAmber
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditEntryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.onPhotoChange(uri.toString())
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Edit ${state.type.label()}" else "Add ${state.type.label()}",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    TonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            FieldLabel("Amount")
            AmountInputField(
                value = TextFieldValue(state.amountText, selection = TextRange(state.amountText.length)),
                onValueChange = { viewModel.onAmountChange(it.text) },
                errorMessage = state.errorMessage?.takeIf { it.contains("mount") }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(500, 1000, 5000, 10000).forEach { amount ->
                    QuickAmountChip(amount = amount) {
                        val current = state.amountText.toDoubleOrNull() ?: 0.0
                        viewModel.onAmountChange(formatAmount(current + amount))
                    }
                }
            }

            FieldLabel("Date", modifier = Modifier.padding(top = 22.dp))
            DatePickerField(date = state.date, onDateChange = viewModel::onDateChange)

            if (state.type == EntryType.EXPENSE) {
                FieldLabel("Category", modifier = Modifier.padding(top = 22.dp))
                CategoryPickerGrid(
                    categories = state.categories,
                    selectedCategoryId = state.categoryId,
                    onSelect = { viewModel.onCategorySelect(it.id) }
                )
                val categoryError = state.errorMessage
                if (categoryError != null && categoryError.contains("ategory")) {
                    Text(
                        categoryError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            FieldLabel("Note (optional)", modifier = Modifier.padding(top = 22.dp))
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                singleLine = true,
                placeholder = { Text("What was this for?") },
                modifier = Modifier.fillMaxWidth()
            )

            FieldLabel("Receipt photo (optional)", modifier = Modifier.padding(top = 22.dp))
            val photo = state.photoUri
            if (photo.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                        .background(SurfaceGlass)
                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add photo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(14.dp))) {
                    coil.compose.AsyncImage(
                        model = photo,
                        contentDescription = "Receipt photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.onPhotoChange(null) },
                        modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) { Text("Remove", color = Color.White) }
                }
            }

            GradientButton(
                text = if (state.isEditing) "Save changes" else "Add ${state.type.label()}",
                onClick = viewModel::save,
                modifier = Modifier.padding(top = 26.dp)
            )

            TipCard(
                text = if (state.type == EntryType.EXPENSE)
                    "Log expenses right after you spend, so your budgets and category breakdown always stay accurate."
                else
                    "Log income as soon as you receive it, so your balance always reflects what's actually in your account.",
                modifier = Modifier.padding(top = 18.dp)
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TipCard(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceGlass, MaterialTheme.shapes.large)
            .border(1.dp, BorderGlass, MaterialTheme.shapes.large)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(AccentAmber.copy(alpha = 0.15f), MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatAmount(value: Double): String {
    val rounded = Math.round(value * 100) / 100.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

private fun EntryType.label(): String = when (this) {
    EntryType.EXPENSE -> "Expense"
    EntryType.INCOME -> "Income"
}
