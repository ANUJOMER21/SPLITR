package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.domain.model.EntryType
import com.omer.expensetracker.ui.theme.BackgroundBase

@Composable
fun EntryTypeChoiceDialog(
    onChoose: (EntryType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundBase,
        title = { Text("Add entry") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { onChoose(EntryType.EXPENSE) }) {
                    Icon(Icons.Filled.TrendingDown, contentDescription = null)
                    Text(" Expense")
                }
                OutlinedButton(onClick = { onChoose(EntryType.INCOME) }) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null)
                    Text(" Income")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
