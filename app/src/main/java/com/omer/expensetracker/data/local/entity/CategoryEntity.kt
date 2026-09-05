package com.omer.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val colorArgb: Long,
    val isDefault: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
