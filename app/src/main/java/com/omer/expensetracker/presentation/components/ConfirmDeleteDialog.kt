package com.omer.expensetracker.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.omer.expensetracker.ui.theme.BackgroundBase

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundBase,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = hapticClick(onConfirm, HapticFeedbackType.LongPress)) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = hapticClick(onDismiss)) { Text("Cancel") }
        }
    )
}
