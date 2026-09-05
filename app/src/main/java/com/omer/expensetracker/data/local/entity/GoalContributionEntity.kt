package com.omer.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "goal_contributions",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId"), Index("isDeleted")]
)
data class GoalContributionEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val amountMinor: Long,
    val dateEpochDay: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
