package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY id DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items")
    suspend fun getItemsList(): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun update(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM items")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getCount(): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<SaleTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: SaleTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<SaleTransactionEntity>)

    @Update
    suspend fun update(transaction: SaleTransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions WHERE itemName IN (:names)")
    suspend fun deleteSampleTransactions(names: List<String>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("UPDATE transactions SET isSettled = 1, settledAt = :settledAt WHERE id = :id")
    suspend fun settleTransaction(id: Long, settledAt: Long)

    @Query("UPDATE transactions SET isSettled = 1, settledAt = :settledAt WHERE customerName = :customerName AND isDebt = 1 AND isSettled = 0")
    suspend fun settleCustomerDebts(customerName: String, settledAt: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM expenses WHERE title IN (:titles)")
    suspend fun deleteSampleExpenses(titles: List<String>)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getCount(): Int
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 300")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun getCount(): Int
}
