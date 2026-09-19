package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val unitPrice: Double,
    val colorHex: String = "#10B981",
    val sku: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class SaleTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long = 0,
    val itemName: String,
    val category: String = "",
    val quantity: Int = 1,
    val unitPrice: Double,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val isDebt: Boolean = false,
    val customerName: String = "",
    val customerPhone: String = "",
    val isSettled: Boolean = false,
    val settledAt: Long? = null
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val title: String,
    val details: String
)
