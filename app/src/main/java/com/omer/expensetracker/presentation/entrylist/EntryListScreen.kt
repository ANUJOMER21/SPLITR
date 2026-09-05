package com.omer.expensetracker.presentation.entrylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.presentation.components.ConfirmDeleteDialog
import com.omer.expensetracker.presentation.components.EmptyState
import com.omer.expensetracker.presentation.components.EntryListRow
import com.omer.expensetracker.presentation.components.EntryTypeChoiceDialog
import com.omer.expensetracker.presentation.components.TonalIconButton
import com.omer.expensetracker.presentation.components.pressScale
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.ExpenseRed
import com.omer.expensetracker.ui.theme.IncomeGreen
import com.omer.expensetracker.ui.theme.SurfaceGlass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    onEntryClick: (entryId: String) -> Unit,
    onAddEntry: (EntryType) -> Unit,
    viewModel: EntryListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showTypeChoice by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(state.lastDeletedEntryId) {
        val id = state.lastDeletedEntryId ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Entry deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        } else {
            viewModel.consumeUndoPrompt()
        }
    }

    val visibleEntries = state.sections.flatMap { it.entries }
    val visibleExpense = visibleEntries.filter { it.type == EntryType.EXPENSE }.sumOf { it.amountMinor }
    val visibleIncome = visibleEntries.filter { it.type == EntryType.INCOME }.sumOf { it.amountMinor }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("Entries", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { GradientFab(onClick = { showTypeChoice = true }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = state.filter.searchQuery,
                    onValueChange = { viewModel.updateFilter(state.filter.copy(searchQuery = it)) },
                    placeholder = { Text("Search category or amount") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceGlass,
                        unfocusedContainerColor = SurfaceGlass,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )
                TonalIconButton(onClick = { showFilterSheet = true }) {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Filter entries",
                        tint = if (state.filter.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (visibleEntries.isNotEmpty()) {
                EntrySummaryStrip(
                    count = visibleEntries.size,
                    expenseMinor = visibleExpense,
                    incomeMinor = visibleIncome,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when {
                state.isLoading -> Unit
                state.sections.isEmpty() && !state.filter.isActive -> EmptyState(
                    title = "No entries yet",
                    subtitle = "Add your first expense or income to get started"
                )
                state.sections.isEmpty() -> EmptyState(
                    title = "No matching entries",
                    subtitle = "Try adjusting your filters or search"
                )
                else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    state.sections.forEach { section ->
                        item(key = "header_${section.label}") {
                            SectionHeader(section.label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        items(section.entries, key = { it.id }) { entry ->
                            EntryListRow(
                                entry = entry,
                                category = entry.categoryId?.let { state.categoriesById[it] },
                                onClick = { onEntryClick(entry.id) },
                                onLongClick = { viewModel.requestDelete(entry.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.pendingDeleteEntryId != null) {
        ConfirmDeleteDialog(
            title = "Delete entry?",
            message = "This entry will be removed. You can undo right after.",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }

    if (showFilterSheet) {
        FilterSheet(
            filter = state.filter,
            categories = state.filterableCategories,
            onApply = { viewModel.updateFilter(it) },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showTypeChoice) {
        EntryTypeChoiceDialog(
            onChoose = {
                showTypeChoice = false
                onAddEntry(it)
            },
            onDismiss = { showTypeChoice = false }
        )
    }
}

@Composable
private fun EntrySummaryStrip(count: Int, expenseMinor: Long, incomeMinor: Long, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("$count ${if (count == 1) "entry" else "entries"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "-${expenseMinor.formatAsCurrency()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ExpenseRed
                )
            }
            if (incomeMinor > 0L) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Income", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "+${incomeMinor.formatAsCurrency()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = IncomeGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun GradientFab(onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(60.dp)
            .pressScale(interactionSource)
            .background(Brush.linearGradient(listOf(AccentBlueDeep, AccentBlue)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Filled.Add, contentDescription = "Add entry", tint = Color.White, modifier = Modifier.height(28.dp))
            }
        }
    }
}
