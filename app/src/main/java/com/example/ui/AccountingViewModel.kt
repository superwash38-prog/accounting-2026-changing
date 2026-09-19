package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class AccountingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AccountingRepository

    val activeTab = MutableStateFlow(AccountingTab.OVERVIEW)
    val timeRange = MutableStateFlow(TimeRangeFilter.ALL_TIME)
    val currencySymbol = MutableStateFlow("ل.س")
    val searchQuery = MutableStateFlow("")

    // Track previous backup before restore so user can easily roll back if desired
    val previousBackupJson = MutableStateFlow<String?>(null)

    val items: StateFlow<List<ItemEntity>>
    val transactions: StateFlow<List<SaleTransactionEntity>>
    val expenses: StateFlow<List<ExpenseEntity>>
    val auditLogs: StateFlow<List<AuditLogEntity>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AccountingRepository(db)

        items = repository.items.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        transactions = repository.transactions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        expenses = repository.expenses.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        auditLogs = repository.auditLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Filter transactions and expenses based on selected time range
    val filteredTransactions = combine(transactions, timeRange) { txList, range ->
        filterByTimeRange(txList, range) { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpenses = combine(expenses, timeRange) { expList, range ->
        filterByTimeRange(expList, range) { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics calculations
    val profitCalculation = combine(filteredTransactions, filteredExpenses) { txs, exps ->
        var accrualRevenue = 0.0
        var cashRevenue = 0.0
        var unpaidDebt = 0.0
        var unitsSold = 0

        val itemSalesMap = mutableMapOf<String, Double>()

        txs.forEach { tx ->
            accrualRevenue += tx.totalAmount
            unitsSold += tx.quantity

            if (tx.isDebt && !tx.isSettled) {
                unpaidDebt += tx.totalAmount
            } else {
                cashRevenue += tx.totalAmount
            }

            val currentItemRev = itemSalesMap.getOrDefault(tx.itemName, 0.0)
            itemSalesMap[tx.itemName] = currentItemRev + tx.totalAmount
        }

        val totalExp = exps.sumOf { it.amount }
        val netCash = cashRevenue - totalExp
        val avgOrder = if (txs.isNotEmpty()) accrualRevenue / txs.size else 0.0

        val topItem = itemSalesMap.maxByOrNull { it.value }
        val topItemName = topItem?.key ?: ""
        val topItemRevenue = topItem?.value ?: 0.0

        ProfitCalculation(
            totalCashRevenue = cashRevenue,
            totalAccrualRevenue = accrualRevenue,
            totalExpenses = totalExp,
            totalUnpaidDebt = unpaidDebt,
            netCashProfit = netCash,
            totalUnitsSold = unitsSold,
            averageOrderValue = avgOrder,
            topItemName = topItemName,
            topItemRevenue = topItemRevenue
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProfitCalculation(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, "", 0.0)
    )

    // Item sales breakdown for donut chart and stats
    val itemSalesSummaries = combine(items, filteredTransactions) { itemList, txList ->
        val totalRevenue = txList.sumOf { it.totalAmount }
        val groupedByItem = txList.groupBy { it.itemId }

        val summaries = itemList.map { item ->
            val matchingTxs = groupedByItem[item.id] ?: emptyList()
            val units = matchingTxs.sumOf { it.quantity }
            val rev = matchingTxs.sumOf { it.totalAmount }
            val pct = if (totalRevenue > 0) (rev / totalRevenue * 100).toFloat() else 0f

            ItemSalesSummary(
                itemId = item.id,
                itemName = item.name,
                category = item.category,
                unitPrice = item.unitPrice,
                totalUnitsSold = units,
                totalRevenue = rev,
                percentageOfTotal = pct,
                colorHex = item.colorHex,
                rank = 0
            )
        }.sortedByDescending { it.totalRevenue }

        summaries.mapIndexed { index, s -> s.copy(rank = index + 1) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grouped customer debt summaries
    val customerDebtSummaries = transactions.map { allTxs ->
        val debtTxs = allTxs.filter { it.isDebt && it.customerName.isNotBlank() }
        val grouped = debtTxs.groupBy { it.customerName }

        grouped.map { (custName, txs) ->
            val unpaid = txs.filter { !it.isSettled }
            val settled = txs.filter { it.isSettled }
            val phone = txs.firstOrNull { it.customerPhone.isNotBlank() }?.customerPhone ?: ""

            CustomerDebtSummary(
                customerName = custName,
                customerPhone = phone,
                totalUnpaidDebt = unpaid.sumOf { it.totalAmount },
                totalSettledDebt = settled.sumOf { it.totalAmount },
                unpaidCount = unpaid.size,
                transactions = txs
            )
        }.sortedByDescending { it.totalUnpaidDebt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun recordQuickSale(item: ItemEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.recordQuickSale(item)
            onComplete()
        }
    }

    fun recordSale(
        item: ItemEntity,
        quantity: Int,
        isDebt: Boolean,
        customerName: String,
        customerPhone: String,
        note: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordSale(
                item = item,
                quantity = quantity,
                isDebt = isDebt,
                customerName = customerName,
                customerPhone = customerPhone,
                note = note
            )
            onComplete()
        }
    }

    fun recordManualDebt(
        customerName: String,
        customerPhone: String,
        amount: Double,
        title: String,
        note: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordManualDebt(
                customerName = customerName,
                customerPhone = customerPhone,
                amount = amount,
                title = title,
                note = note
            )
            onComplete()
        }
    }

    fun settleTransaction(id: Long, customerName: String, amount: Double) {
        viewModelScope.launch {
            repository.settleTransaction(id, customerName, amount)
        }
    }

    fun settleCustomerDebts(customerName: String) {
        viewModelScope.launch {
            repository.settleCustomerDebts(customerName)
        }
    }

    fun deleteTransaction(tx: SaleTransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx.id, tx.itemName)
        }
    }

    fun addItem(
        name: String,
        category: String,
        unitPrice: Double,
        colorHex: String,
        sku: String,
        description: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.insertItem(
                ItemEntity(
                    name = name.trim(),
                    category = category.trim(),
                    unitPrice = unitPrice,
                    colorHex = colorHex,
                    sku = sku.trim(),
                    description = description.trim()
                )
            )
            onComplete()
        }
    }

    fun updateItem(item: ItemEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateItem(item)
            onComplete()
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item.id, item.name)
        }
    }

    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        note: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.insertExpense(title, category, amount, note)
            onComplete()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense.id, expense.title)
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            repository.clearAuditLogs()
        }
    }

    fun clearAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllData()
            onComplete()
        }
    }

    suspend fun getExportJson(): String {
        return repository.exportJsonBackup(
            items.value,
            transactions.value,
            expenses.value
        )
    }

    fun restoreBackup(jsonStr: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Automatically capture current snapshot before replacing so user can go back anytime
            val currentSnapshot = repository.exportJsonBackup(
                items.value,
                transactions.value,
                expenses.value
            )
            val success = repository.restoreJsonBackup(jsonStr)
            if (success) {
                previousBackupJson.value = currentSnapshot
            }
            onResult(success)
        }
    }

    fun rollbackPreviousBackup(onResult: (Boolean) -> Unit) {
        val prev = previousBackupJson.value ?: return onResult(false)
        viewModelScope.launch {
            val currentSnapshot = repository.exportJsonBackup(
                items.value,
                transactions.value,
                expenses.value
            )
            val success = repository.restoreJsonBackup(prev)
            if (success) {
                previousBackupJson.value = currentSnapshot
            }
            onResult(success)
        }
    }

    private fun <T> filterByTimeRange(
        list: List<T>,
        range: TimeRangeFilter,
        getTimestamp: (T) -> Long
    ): List<T> {
        if (range == TimeRangeFilter.ALL_TIME) return list

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val startOfDay = cal.timeInMillis

        return when (range) {
            TimeRangeFilter.ALL_TIME -> list
            TimeRangeFilter.TODAY -> list.filter { getTimestamp(it) >= startOfDay }
            TimeRangeFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val startOfWeek = cal.timeInMillis
                list.filter { getTimestamp(it) >= startOfWeek }
            }
            TimeRangeFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = cal.timeInMillis
                list.filter { getTimestamp(it) >= startOfMonth }
            }
        }
    }
}
