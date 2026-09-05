package com.omer.expensetracker.presentation.notifications

import androidx.compose.ui.graphics.vector.ImageVector

enum class NotificationSeverity { CRITICAL, WARNING, INFO }

data class NotificationItem(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val severity: NotificationSeverity
)

data class NotificationsUiState(
    val items: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = true
)
