package com.omer.expensetracker.data.local.dao.split

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omer.expensetracker.data.local.entity.split.ExpenseItemEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseItemShareEntity
import com.omer.expensetracker.data.local.entity.split.ExpensePayerEntity
import com.omer.expensetracker.data.local.entity.split.ExpenseSplitEntity
import com.omer.expensetracker.data.local.entity.split.SharedExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedExpenseDao {
    @Query("SELECT * FROM shared_expenses WHERE isDeleted = 0 ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<SharedExpenseEntity>>

    @Query("SELECT * FROM shared_expenses WHERE isDeleted = 0 AND groupId = :groupId ORDER BY dateEpochDay DESC")
    fun observeForGroup(groupId: String): Flow<List<SharedExpenseEntity>>

    @Query(
        """
        SELECT * FROM shared_expenses WHERE isDeleted = 0 AND id IN (
            SELECT expenseId FROM expense_payers WHERE friendId = :friendId
            UNION
            SELECT expenseId FROM expense_splits WHERE friendId = :friendId
        ) ORDER BY dateEpochDay DESC
        """
    )
    fun observeForFriend(friendId: String): Flow<List<SharedExpenseEntity>>

    @Query("SELECT * FROM shared_expenses WHERE id = :id")
    suspend fun getById(id: String): SharedExpenseEntity?

    @Insert
    suspend fun insertExpense(expense: SharedExpenseEntity)

    @Update
    suspend fun updateExpense(expense: SharedExpenseEntity)

    @Query("UPDATE shared_expenses SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteExpense(id: String, updatedAt: Long)

    // ---- payers ----

    @Query("SELECT * FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun getPayers(expenseId: String): List<ExpensePayerEntity>

    @Query("SELECT * FROM expense_payers WHERE friendId = :friendId")
    suspend fun getPayersForFriend(friendId: String): List<ExpensePayerEntity>

    @Insert
    suspend fun insertPayers(payers: List<ExpensePayerEntity>)

    @Query("DELETE FROM expense_payers WHERE expenseId = :expenseId")
    suspend fun deletePayers(expenseId: String)

    // ---- splits ----

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun getSplits(expenseId: String): List<ExpenseSplitEntity>

    @Query("SELECT * FROM expense_splits WHERE friendId = :friendId")
    suspend fun getSplitsForFriend(friendId: String): List<ExpenseSplitEntity>

    @Insert
    suspend fun insertSplits(splits: List<ExpenseSplitEntity>)

    @Query("DELETE FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteSplits(expenseId: String)

    // ---- itemized ----

    @Query("SELECT * FROM expense_items WHERE expenseId = :expenseId")
    suspend fun getItems(expenseId: String): List<ExpenseItemEntity>

    @Insert
    suspend fun insertItems(items: List<ExpenseItemEntity>)

    @Query("DELETE FROM expense_items WHERE expenseId = :expenseId")
    suspend fun deleteItems(expenseId: String)

    @Query("SELECT * FROM expense_item_shares WHERE itemId IN (SELECT id FROM expense_items WHERE expenseId = :expenseId)")
    suspend fun getItemShares(expenseId: String): List<ExpenseItemShareEntity>

    @Insert
    suspend fun insertItemShares(shares: List<ExpenseItemShareEntity>)

    @Query("DELETE FROM expense_item_shares WHERE itemId IN (SELECT id FROM expense_items WHERE expenseId = :expenseId)")
    suspend fun deleteItemShares(expenseId: String)
}
