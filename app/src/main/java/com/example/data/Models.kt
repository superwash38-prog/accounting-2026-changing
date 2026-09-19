package com.example.data

enum class TimeRangeFilter(val labelAr: String, val labelEn: String) {
    ALL_TIME("كافة الأوقات", "All Time"),
    TODAY("اليوم", "Today"),
    THIS_WEEK("هذا الأسبوع", "This Week"),
    THIS_MONTH("هذا الشهر", "This Month")
}

enum class AccountingTab(val titleAr: String, val titleEn: String) {
    OVERVIEW("المؤشرات", "Overview"),
    ITEMS("المواد والخدمات", "Items"),
    DEBTS("الديون", "Debts"),
    EXPENSES("المصاريف", "Expenses"),
    LEDGER("السجل المالي", "Ledger"),
    SETTINGS("الإعدادات والنسخ", "Settings")
}

data class ItemSalesSummary(
    val itemId: Long,
    val itemName: String,
    val category: String,
    val unitPrice: Double,
    val totalUnitsSold: Int,
    val totalRevenue: Double,
    val percentageOfTotal: Float,
    val colorHex: String,
    val rank: Int
)

data class CustomerDebtSummary(
    val customerName: String,
    val customerPhone: String,
    val totalUnpaidDebt: Double,
    val totalSettledDebt: Double,
    val unpaidCount: Int,
    val transactions: List<SaleTransactionEntity>
)

data class ProfitCalculation(
    val totalCashRevenue: Double,
    val totalAccrualRevenue: Double,
    val totalExpenses: Double,
    val totalUnpaidDebt: Double,
    val netCashProfit: Double,
    val totalUnitsSold: Int,
    val averageOrderValue: Double,
    val topItemName: String,
    val topItemRevenue: Double
)
