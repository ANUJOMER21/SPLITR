package com.omer.expensetracker.data.local.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.omer.expensetracker.data.local.entity.GoalContributionEntity
import com.omer.expensetracker.data.local.entity.SavingsGoalEntity

data class GoalWithContributions(
    @Embedded val goal: SavingsGoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val contributions: List<GoalContributionEntity>
) {
    /** [Relation] doesn't apply the child's own soft-delete filter — this view already
     * needs it whenever it computes a total, so keep the raw list private-ish and expose
     * this instead. */
    val activeContributions: List<GoalContributionEntity> get() = contributions.filter { !it.isDeleted }
}
