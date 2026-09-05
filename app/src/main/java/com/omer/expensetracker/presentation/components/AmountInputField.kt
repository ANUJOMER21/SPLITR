package com.omer.expensetracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.ui.theme.AccentBlue
import com.omer.expensetracker.ui.theme.AccentBlueDeep
import com.omer.expensetracker.ui.theme.AccentCyan
import com.omer.expensetracker.ui.theme.BorderGlass
import com.omer.expensetracker.ui.theme.SurfaceGlass
import com.omer.expensetracker.ui.theme.TextTertiary

/** A glass "amount" field — a rupee chip on a gradient badge plus a large borderless number
 * field, matching the reference design's `.amount-card`. */
@Composable
fun AmountInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceGlass, MaterialTheme.shapes.large)
                .border(1.dp, if (errorMessage != null) MaterialTheme.colorScheme.error else BorderGlass, MaterialTheme.shapes.large)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(listOf(AccentBlueDeep, AccentCyan)), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("₹", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            TextField(
                value = value,
                onValueChange = { new ->
                    val filtered = new.text.filter { it.isDigit() || it == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        onValueChange(new.copy(text = filtered))
                    }
                },
                placeholder = {
                    Text(
                        "0.00",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextTertiary
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = AccentBlue
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .semantics { contentDescription = "Amount input" }
            )
        }
        if (errorMessage != null) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }
    }
}
