package com.omer.expensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** [id] is a synthetic key (`friendId|groupId-or-"overall"`) since Room primary keys can't be
 * partially null; [friendId] and [groupId] remain plain columns for querying. */
@Entity(tableName = "balances", indices = [Index("friendId"), Index("groupId")])
data class BalanceEntity(
    @PrimaryKey val id: String,
    val friendId: String,
    val groupId: String?,
    val netMinor: Long
) {
    companion object {
        fun makeId(friendId: String, groupId: String?) = "$friendId|${groupId ?: "overall"}"
    }
}
