package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class AccountingRepository(private val db: AppDatabase) {
    val items: Flow<List<ItemEntity>> = db.itemDao().getAllItems()
    val transactions: Flow<List<SaleTransactionEntity>> = db.transactionDao().getAllTransactions()
    val expenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    val auditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllLogs()

    suspend fun insertItem(item: ItemEntity): Long {
        val id = db.itemDao().insert(item)
        logAction("ITEM_CREATED", "إضافة مادة/خدمة", "تمت إضافة المادة: ${item.name} بسعر ${item.unitPrice}")
        return id
    }

    suspend fun updateItem(item: ItemEntity) {
        db.itemDao().update(item)
        logAction("ITEM_UPDATED", "تعديل مادة", "تم تعديل المادة: ${item.name}")
    }

    suspend fun deleteItem(id: Long, name: String) {
        db.itemDao().deleteById(id)
        logAction("ITEM_DELETED", "حذف مادة", "تم حذف المادة: $name")
    }

    suspend fun recordSale(
        item: ItemEntity,
        quantity: Int,
        isDebt: Boolean,
        customerName: String,
        customerPhone: String,
        note: String
    ): Long {
        val total = item.unitPrice * quantity
        val tx = SaleTransactionEntity(
            itemId = item.id,
            itemName = item.name,
            category = item.category,
            quantity = quantity,
            unitPrice = item.unitPrice,
            totalAmount = total,
            timestamp = System.currentTimeMillis(),
            note = note,
            isDebt = isDebt,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            isSettled = !isDebt,
            settledAt = if (!isDebt) System.currentTimeMillis() else null
        )
        val id = db.transactionDao().insert(tx)
        if (isDebt) {
            logAction("DEBT_RECORDED", "تسجيل دين جديد", "دين على العميل: $customerName بمبلغ $total للمادة ${item.name}")
        } else {
            logAction("SALE_CREATED", "تسجيل عملية بيع", "بيع $quantity × ${item.name} بمبلغ $total نقداً")
        }
        return id
    }

    suspend fun recordQuickSale(item: ItemEntity): Long {
        return recordSale(
            item = item,
            quantity = 1,
            isDebt = false,
            customerName = "",
            customerPhone = "",
            note = "بيع فوري سريع"
        )
    }

    suspend fun recordManualDebt(
        customerName: String,
        customerPhone: String,
        amount: Double,
        title: String,
        note: String
    ): Long {
        val tx = SaleTransactionEntity(
            itemId = 0,
            itemName = if (title.isBlank()) "دين مباشر على الحساب" else title.trim(),
            category = "ديون مباشرة",
            quantity = 1,
            unitPrice = amount,
            totalAmount = amount,
            timestamp = System.currentTimeMillis(),
            note = note.trim(),
            isDebt = true,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            isSettled = false,
            settledAt = null
        )
        val id = db.transactionDao().insert(tx)
        logAction("DEBT_RECORDED", "تسجيل دين يدوي", "دين يدوي على: $customerName بمبلغ $amount ($title)")
        return id
    }

    suspend fun settleTransaction(id: Long, customerName: String, amount: Double) {
        db.transactionDao().settleTransaction(id, System.currentTimeMillis())
        logAction("DEBT_SETTLED", "تسديد دين", "تم تسديد دين للعميل: $customerName بقيمة $amount")
    }

    suspend fun settleCustomerDebts(customerName: String) {
        db.transactionDao().settleCustomerDebts(customerName, System.currentTimeMillis())
        logAction("DEBT_SETTLED", "تسديد كامل ديون العميل", "تم تسديد كافة ديون العميل: $customerName")
    }

    suspend fun deleteTransaction(id: Long, itemName: String) {
        db.transactionDao().deleteById(id)
        logAction("TRANSACTION_DELETED", "حذف معاملة", "تم حذف المعاملة: $itemName (معرف #$id)")
    }

    suspend fun insertExpense(title: String, category: String, amount: Double, note: String): Long {
        val expense = ExpenseEntity(
            title = title.trim(),
            category = category.trim(),
            amount = amount,
            timestamp = System.currentTimeMillis(),
            note = note.trim()
        )
        val id = db.expenseDao().insert(expense)
        logAction("EXPENSE_CREATED", "تسجيل مصروف", "تسجيل مصروف: $title بمبلغ $amount في تصنيف $category")
        return id
    }

    suspend fun deleteExpense(id: Long, title: String) {
        db.expenseDao().deleteById(id)
        logAction("EXPENSE_DELETED", "حذف مصروف", "تم حذف المصروف: $title")
    }

    suspend fun logAction(action: String, title: String, details: String) {
        db.auditLogDao().insert(
            AuditLogEntity(
                action = action,
                title = title,
                details = details,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearAuditLogs() {
        db.auditLogDao().clearAll()
    }

    suspend fun exportJsonBackup(
        items: List<ItemEntity>,
        transactions: List<SaleTransactionEntity>,
        expenses: List<ExpenseEntity>
    ): String {
        val root = JSONObject()
        root.put("version", 2000)
        root.put("app", "Accounting House")
        root.put("exportedAt", System.currentTimeMillis())

        val itemsArr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("category", item.category)
            obj.put("unitPrice", item.unitPrice)
            obj.put("colorHex", item.colorHex)
            obj.put("sku", item.sku)
            obj.put("description", item.description)
            obj.put("createdAt", item.createdAt)
            itemsArr.put(obj)
        }
        root.put("items", itemsArr)

        val txArr = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("itemId", tx.itemId)
            obj.put("itemName", tx.itemName)
            obj.put("category", tx.category)
            obj.put("quantity", tx.quantity)
            obj.put("unitPrice", tx.unitPrice)
            obj.put("totalAmount", tx.totalAmount)
            obj.put("timestamp", tx.timestamp)
            obj.put("note", tx.note)
            obj.put("isDebt", tx.isDebt)
            obj.put("customerName", tx.customerName)
            obj.put("customerPhone", tx.customerPhone)
            obj.put("isSettled", tx.isSettled)
            if (tx.settledAt != null) obj.put("settledAt", tx.settledAt)
            txArr.put(obj)
        }
        root.put("transactions", txArr)

        val expArr = JSONArray()
        expenses.forEach { exp ->
            val obj = JSONObject()
            obj.put("id", exp.id)
            obj.put("title", exp.title)
            obj.put("category", exp.category)
            obj.put("amount", exp.amount)
            obj.put("timestamp", exp.timestamp)
            obj.put("note", exp.note)
            expArr.put(obj)
        }
        root.put("expenses", expArr)

        return root.toString(2)
    }

    suspend fun restoreJsonBackup(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            val newItems = mutableListOf<ItemEntity>()
            if (root.has("items")) {
                val itemsArr = root.getJSONArray("items")
                for (i in 0 until itemsArr.length()) {
                    val obj = itemsArr.getJSONObject(i)
                    newItems.add(
                        ItemEntity(
                            id = if (obj.has("id")) obj.getLong("id") else 0L,
                            name = obj.getString("name"),
                            category = obj.optString("category", "عام"),
                            unitPrice = obj.optDouble("unitPrice", 0.0),
                            colorHex = obj.optString("colorHex", "#10B981"),
                            sku = obj.optString("sku", ""),
                            description = obj.optString("description", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val newTxs = mutableListOf<SaleTransactionEntity>()
            if (root.has("transactions")) {
                val txArr = root.getJSONArray("transactions")
                for (i in 0 until txArr.length()) {
                    val obj = txArr.getJSONObject(i)
                    newTxs.add(
                        SaleTransactionEntity(
                            id = if (obj.has("id")) obj.getLong("id") else 0L,
                            itemId = obj.optLong("itemId", 0L),
                            itemName = obj.getString("itemName"),
                            category = obj.optString("category", "عام"),
                            quantity = obj.optInt("quantity", 1),
                            unitPrice = obj.optDouble("unitPrice", 0.0),
                            totalAmount = obj.optDouble("totalAmount", 0.0),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            note = obj.optString("note", ""),
                            isDebt = obj.optBoolean("isDebt", false),
                            customerName = obj.optString("customerName", ""),
                            customerPhone = obj.optString("customerPhone", ""),
                            isSettled = obj.optBoolean("isSettled", false),
                            settledAt = if (obj.has("settledAt") && !obj.isNull("settledAt")) obj.getLong("settledAt") else null
                        )
                    )
                }
            }

            val newExps = mutableListOf<ExpenseEntity>()
            if (root.has("expenses")) {
                val expArr = root.getJSONArray("expenses")
                for (i in 0 until expArr.length()) {
                    val obj = expArr.getJSONObject(i)
                    newExps.add(
                        ExpenseEntity(
                            id = if (obj.has("id")) obj.getLong("id") else 0L,
                            title = obj.getString("title"),
                            category = obj.optString("category", "مصروف عام"),
                            amount = obj.optDouble("amount", 0.0),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            // Replace current data atomically
            db.itemDao().deleteAll()
            db.transactionDao().deleteAll()
            db.expenseDao().deleteAll()

            if (newItems.isNotEmpty()) db.itemDao().insertAll(newItems)
            if (newTxs.isNotEmpty()) db.transactionDao().insertAll(newTxs)
            if (newExps.isNotEmpty()) db.expenseDao().insertAll(newExps)

            logAction(
                "BACKUP_RESTORED",
                "استعادة نسخة احتياطية",
                "تم استرداد ${newItems.size} مادة، ${newTxs.size} عملية، و${newExps.size} سند صرف بنجاح"
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        // Automatically purge any previously seeded dummy services/items so existing users get a clean slate immediately
        val sampleSkus = listOf("CONS-01", "AUDIT-02", "TAX-03", "BOOK-04", "SYS-05")
        val sampleNames = listOf(
            "استشارة محاسبية ودراسة جدوى",
            "تدقيق حسابات وميزانية سنوية",
            "إعداد وتدقيق الإقرار الضريبي",
            "باقة إدخال القيود اليومية (شهري)",
            "نظام الفوترة والمحاسبة السحابية"
        )
        val allItems = db.itemDao().getItemsList()
        val hasSampleItems = allItems.any { it.sku in sampleSkus || it.name in sampleNames }

        if (hasSampleItems) {
            // Delete sample items and their sample transactions
            for (item in allItems) {
                if (item.sku in sampleSkus || item.name in sampleNames) {
                    db.itemDao().deleteById(item.id)
                }
            }
            // Delete sample transactions and expenses if present
            db.transactionDao().deleteSampleTransactions(sampleNames)
            db.expenseDao().deleteSampleExpenses(listOf("إيجار المكتب والخدمات العامة", "اشتراك إنترنت ومستلزمات مكتبية"))
            logAction("CLEANUP", "إزالة الخدمات الجاهزة", "تمت إزالة كافة الخدمات الجاهزة والبيانات التجريبية تلقائياً لتفريغ النظام")
        }
    }

    suspend fun clearAllData() {
        db.itemDao().deleteAll()
        db.transactionDao().deleteAll()
        db.expenseDao().deleteAll()
        logAction("DATA_CLEARED", "مسح كافة البيانات", "تم تفريغ كافة المواد والمعاملات والمصاريف من النظام")
    }
}
