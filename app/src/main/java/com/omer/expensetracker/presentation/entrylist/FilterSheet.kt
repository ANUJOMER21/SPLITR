package com.omer.expensetracker.presentation.entrylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.DateRangePreset
import com.omer.expensetracker.domain.model.EntryFilter
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.presentation.components.DatePickerField
import com.omer.expensetracker.presentation.components.FieldLabel
import com.omer.expensetracker.presentation.components.GradientButton
import com.omer.expensetracker.presentation.components.themedSegmentedColors
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentPrimaryContainer
import com.omer.expensetracker.ui.theme.SurfaceGlass
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    filter: EntryFilter,
    categories: List<Category>,
    onApply: (EntryFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf(filter.type) }
    var selectedCategoryIds by remember { mutableStateOf(filter.categoryIds) }
    var preset by remember { mutableStateOf(DateRangePreset.ALL_TIME) }
    var startDate by remember { mutableStateOf(filter.startDate) }
    var endDate by remember { mutableStateOf(filter.endDate) }
    var minAmount by remember { mutableStateOf(filter.minAmountMinor?.let { (it / 100.0).toString() } ?: "") }
    var maxAmount by remember { mutableStateOf(filter.maxAmountMinor?.let { (it / 100.0).toString() } ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text("Filter entries", style = MaterialTheme.typography.titleLarge)

            FieldLabel("Type", modifier = Modifier.padding(top = 22.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf<EntryType?>(null, EntryType.EXPENSE, EntryType.INCOME)
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = type == option,
                        onClick = { type = option },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        colors = themedSegmentedColors()
                    ) {
                        Text(
                            when (option) {
                                null -> "All"
                                EntryType.EXPENSE -> "Expense"
                                EntryType.INCOME -> "Income"
                            }
                        )
                    }
                }
            }

            FieldLabel("Category", modifier = Modifier.padding(top = 22.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it.id }) { category ->
                    val selected = category.id in selectedCategoryIds
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedCategoryIds = if (selected) {
                                selectedCategoryIds - category.id
                            } else {
                                selectedCategoryIds + category.id
                            }
                        },
                        label = { Text(category.name) },
                        colors = filterChipColors(),
                        border = null
                    )
                }
            }

            FieldLabel("Date range", modifier = Modifier.padding(top = 22.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val presets = listOf(
                    DateRangePreset.THIS_WEEK to "This week",
                    DateRangePreset.THIS_MONTH to "This month",
                    DateRangePreset.LAST_MONTH to "Last month",
                    DateRangePreset.THIS_YEAR to "This year",
                    DateRangePreset.ALL_TIME to "All time"
                )
                items(presets, key = { it.first }) { (value, label) ->
                    FilterChip(
                        selected = preset == value,
                        onClick = {
                            preset = value
                            val (s, e) = resolvePresetRange(value)
                            startDate = s
                            endDate = e
                        },
                        label = { Text(label) },
                        colors = filterChipColors(),
                        border = null
                    )
                }
            }
            if (preset == DateRangePreset.CUSTOM || startDate != null || endDate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        date = startDate ?: LocalDate.now(),
                        onDateChange = { startDate = it; preset = DateRangePreset.CUSTOM },
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        date = endDate ?: LocalDate.now(),
                        onDateChange = { endDate = it; preset = DateRangePreset.CUSTOM },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            FieldLabel("Amount range", modifier = Modifier.padding(top = 22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassAmountField(
                    value = minAmount,
                    onValueChange = { minAmount = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = "Min",
                    modifier = Modifier.weight(1f)
                )
                GlassAmountField(
                    value = maxAmount,
                    onValueChange = { maxAmount = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = "Max",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp, bottom = 20.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = {
                    onApply(EntryFilter(searchQuery = filter.searchQuery))
                    onDismiss()
                }) { Text("Clear", color = AccentBlue) }
                GradientButton(
                    text = "Apply",
                    onClick = {
                        onApply(
                            EntryFilter(
                                type = type,
                                categoryIds = selectedCategoryIds,
                                startDate = startDate,
                                endDate = endDate,
                                minAmountMinor = minAmount.toDoubleOrNull()?.let { (it * 100).toLong() },
                                maxAmountMinor = maxAmount.toDoubleOrNull()?.let { (it * 100).toLong() },
                                searchQuery = filter.searchQuery
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = SurfaceGlass,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = AccentPrimaryContainer,
    selectedLabelColor = AccentBlue
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassAmountField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceGlass,
            unfocusedContainerColor = SurfaceGlass,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = modifier
    )
}

private fun resolvePresetRange(preset: DateRangePreset): Pair<LocalDate?, LocalDate?> {
    val today = LocalDate.now()
    return when (preset) {
        DateRangePreset.THIS_WEEK -> today.with(java.time.DayOfWeek.MONDAY) to today
        DateRangePreset.THIS_MONTH -> today.withDayOfMonth(1) to today
        DateRangePreset.LAST_MONTH -> {
            val lastMonth = today.minusMonths(1)
            lastMonth.withDayOfMonth(1) to lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
        }
        DateRangePreset.THIS_YEAR -> today.withDayOfYear(1) to today
        DateRangePreset.ALL_TIME, DateRangePreset.CUSTOM -> null to null
    }
}
