package com.omer.expensetracker.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.omer.expensetracker.domain.model.Category
import com.omer.expensetracker.presentation.util.CategoryIconProvider

@Composable
fun CategoryDot(
    colorArgb: Long,
    iconKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    selected: Boolean = false
) {
    val bg = Color(colorArgb)

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 400f
        ),
        label = "dotScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "dotBorder"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (selected) 10.dp else 3.dp,
                shape = CircleShape,
                spotColor = bg,
                ambientColor = bg
            )
            .clip(CircleShape)
            .background(bg)
            .border(
                width = if (selected) 2.5.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CategoryIconProvider.iconFor(iconKey),
            contentDescription = null,
            tint = if (bg.luminance() > 0.55f) Color.Black.copy(alpha = 0.85f) else Color.White,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPickerGrid(
    categories: List<Category>,
    selectedCategoryId: String?,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val selected = category.id == selectedCategoryId

            CategoryPickerItem(
                name = category.name,
                colorArgb = category.colorArgb,
                iconKey = category.iconKey,
                selected = selected,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun CategoryPickerItem(
    name: String,
    colorArgb: Long,
    iconKey: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable(
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp)
            .semantics {
                contentDescription = "$name category"
                role = Role.RadioButton
            }
    ) {
        // Only the dot is circular
        CategoryDot(
            colorArgb = colorArgb,
            iconKey = iconKey,
            selected = selected,
            size = 48.dp
        )

        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            },
            maxLines = 2,                       // allows longer names
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 6.dp)
                .width(72.dp)
        )
    }
}