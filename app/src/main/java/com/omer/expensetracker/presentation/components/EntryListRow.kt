package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.domain.model.Entry
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.presentation.util.formatAsCurrency
import com.omer.expensetracker.ui.theme.ExpenseRed
import com.omer.expensetracker.ui.theme.IncomeGreen
import java.time.format.DateTimeFormatter

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun EntryListRow(
    entry: Entry,
    category: Category?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
) {
    val amountColor = if (entry.type == EntryType.EXPENSE) MaterialTheme.colorScheme.error else IncomeGreen
    val sign = if (entry.type == EntryType.EXPENSE) "-" else "+"

    ListItemCard(
        onClick = onClick,
        onLongClick = onLongClick,
        paddingValues = paddingValues,
        modifier = modifier.semantics {
            contentDescription = buildString {
                append(if (entry.type == EntryType.EXPENSE) "Expense " else "Income ")
                category?.let { append("in ${it.name} ") }
                append(entry.amountMinor.formatAsCurrency())
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                entry.type == EntryType.EXPENSE && category != null -> CategoryDot(category.colorArgb, category.iconKey, size = 36.dp)
                entry.type == EntryType.EXPENSE -> Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ExpenseRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = ExpenseRed)
                }
                else -> Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(IncomeGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = IncomeGreen)
                }
            }

            val label = if (entry.type == EntryType.EXPENSE) (category?.name ?: "Other") else "Income"
            val note = entry.note?.takeIf { it.isNotBlank() }
            val dateText = entry.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note ?: label,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = if (note != null) "$label · $dateText" else dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$sign${entry.amountMinor.formatAsCurrency()}",
                style = MaterialTheme.typography.bodyLarge,
                color = amountColor
            )
        }
    }
}
