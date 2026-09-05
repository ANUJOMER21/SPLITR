package com.omer.expensetracker.data.mapper

import com.omer.expensetracker.data.local.entity.CategoryEntity
import com.omer.expensetracker.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isDefault = isDefault,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    isDefault = isDefault,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)
