package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.*
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: AccountingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AccountingHouseApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingHouseApp(viewModel: AccountingViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val timeRange by viewModel.timeRange.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    val items by viewModel.items.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val profitCalc by viewModel.profitCalculation.collectAsStateWithLifecycle()
    val itemSummaries by viewModel.itemSalesSummaries.collectAsStateWithLifecycle()
    val customerDebts by viewModel.customerDebtSummaries.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val previousBackupJson by viewModel.previousBackupJson.collectAsStateWithLifecycle()

    // Dialog states
    var saleItemTarget by remember { mutableStateOf<ItemEntity?>(null) }
    var showManualDebtDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ItemEntity?>(null) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showProfitDetailsDialog by remember { mutableStateOf(false) }
    var selectedCustomerForDetails by remember { mutableStateOf<CustomerDebtSummary?>(null) }
    var backupJsonContent by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    // Direct JSON file save launcher (SAF CreateDocument)
    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && backupJsonContent != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(backupJsonContent!!.toByteArray(Charsets.UTF_8))
                }
                scope.launch {
                    snackbarHostState.showSnackbar("تم حفظ وتنزيل ملف النسخة الاحتياطية بنجاح على هاتفك")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                scope.launch {
                    snackbarHostState.showSnackbar("حدث خطأ أثناء حفظ الملف: ${e.localizedMessage}")
                }
            }
        }
    }

    // Direct JSON file picker launcher (SAF OpenDocument)
    val openJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (!jsonString.isNullOrBlank()) {
                    viewModel.restoreBackup(jsonString) { success ->
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("تم استرداد واستعادة قاعدة البيانات من الملف المختار بنجاح!")
                                showRestoreDialog = false
                            } else {
                                snackbarHostState.showSnackbar("فشل الاستيراد: يرجى التأكد من أن الملف هو ملف JSON صالح لبيت المحاسبة")
                            }
                        }
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("الملف المختار فارغ!")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                scope.launch {
                    snackbarHostState.showSnackbar("تعذر قراءة الملف: ${e.localizedMessage}")
                }
            }
        }
    }

    fun downloadAndSaveBackup(json: String) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "accounting_backup_$timeStamp.json"
        try {
            // First: launch system file picker to save file wherever user wants in mobile files
            backupJsonContent = json
            createJsonLauncher.launch(fileName)
        } catch (e: Exception) {
            // Fallback: save to internal downloads and open share sheet
            try {
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                val backupFile = File(downloadsDir, fileName)
                backupFile.writeText(json)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - بيت المحاسبة ($fileName)")
                    putExtra(Intent.EXTRA_TEXT, json)
                }
                context.startActivity(Intent.createChooser(shareIntent, "حفظ أو إرسال ملف النسخة الاحتياطية JSON"))

                scope.launch {
                    snackbarHostState.showSnackbar("تم تجهيز النسخة: ${backupFile.name}")
                }
            } catch (ex: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("تعذر حفظ الملف: ${ex.localizedMessage}")
                }
            }
        }
    }

    var showTimeRangeMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate950,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_accounting_house),
                            contentDescription = "شعار بيت المحاسبة",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                "بيت المحاسبة",
                                color = Slate100,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Accounting House",
                                color = Emerald400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Time Range Selector button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate900,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                            modifier = Modifier.clickable {
                                SoundManager.playClick(context = context)
                                showTimeRangeMenu = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = Emerald400, modifier = Modifier.size(14.dp))
                                Text(timeRange.labelAr, color = Slate200, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = showTimeRangeMenu,
                            onDismissRequest = { showTimeRangeMenu = false }
                        ) {
                            TimeRangeFilter.values().forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(range.labelAr) },
                                    onClick = {
                                        SoundManager.playClick(context = context)
                                        viewModel.timeRange.value = range
                                        showTimeRangeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Settings quick button
                    IconButton(onClick = {
                        SoundManager.playClick(context = context)
                        viewModel.activeTab.value = AccountingTab.SETTINGS
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = if (activeTab == AccountingTab.SETTINGS) Emerald400 else Slate400
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple(AccountingTab.OVERVIEW, "المؤشرات", Icons.Default.Dashboard),
                    Triple(AccountingTab.ITEMS, "المواد", Icons.Default.Inventory2),
                    Triple(AccountingTab.DEBTS, "الديون", Icons.Default.CreditCard),
                    Triple(AccountingTab.EXPENSES, "المصاريف", Icons.Default.ReceiptLong),
                    Triple(AccountingTab.LEDGER, "السجل", Icons.Default.HistoryEdu)
                )

                navItems.forEach { (tab, label, icon) ->
                    val isSelected = activeTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            SoundManager.playClick(context = context)
                            viewModel.activeTab.value = tab
                        },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) Emerald400 else Slate400
                            )
                        },
                        label = {
                            Text(
                                label,
                                color = if (isSelected) Emerald300 else Slate400,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Emerald900.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate950)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                when (activeTab) {
                    AccountingTab.OVERVIEW -> {
                        OverviewScreen(
                            calc = profitCalc,
                            items = items,
                            summaries = itemSummaries,
                            currencySymbol = currencySymbol,
                            onShowProfitDetails = {
                                SoundManager.playClick(context = context)
                                showProfitDetailsDialog = true
                            },
                            onQuickSale = { item ->
                                SoundManager.playCash(context = context)
                                viewModel.recordQuickSale(item) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("تم تسجيل بيع فوري: ${item.name}")
                                    }
                                }
                            },
                            onOpenSaleDialog = { item ->
                                SoundManager.playClick(context = context)
                                saleItemTarget = item
                            },
                            onNavigateToTab = { targetTab ->
                                SoundManager.playClick(context = context)
                                viewModel.activeTab.value = targetTab
                            }
                        )
                    }

                    AccountingTab.ITEMS -> {
                        ItemsScreen(
                            items = items,
                            summaries = itemSummaries,
                            currencySymbol = currencySymbol,
                            onAddNewItem = {
                                SoundManager.playClick(context = context)
                                showAddItemDialog = true
                            },
                            onEditItem = { item ->
                                SoundManager.playClick(context = context)
                                itemToEdit = item
                            },
                            onDeleteItem = { item ->
                                SoundManager.playDelete(context = context)
                                viewModel.deleteItem(item)
                            },
                            onSellItem = { item ->
                                SoundManager.playClick(context = context)
                                saleItemTarget = item
                            }
                        )
                    }

                    AccountingTab.DEBTS -> {
                        DebtsScreen(
                            customerDebts = customerDebts,
                            currencySymbol = currencySymbol,
                            onRecordManualDebt = {
                                SoundManager.playClick(context = context)
                                showManualDebtDialog = true
                            },
                            onSelectCustomer = { summary ->
                                SoundManager.playClick(context = context)
                                selectedCustomerForDetails = summary
                            },
                            onSettleAllDebts = { customerName ->
                                SoundManager.playCash(context = context)
                                viewModel.settleCustomerDebts(customerName)
                                scope.launch {
                                    snackbarHostState.showSnackbar("تم تسديد كامل ذمة العميل: $customerName")
                                }
                            }
                        )
                    }

                    AccountingTab.EXPENSES -> {
                        ExpensesScreen(
                            expenses = expenses,
                            currencySymbol = currencySymbol,
                            onAddExpense = {
                                SoundManager.playClick(context = context)
                                showAddExpenseDialog = true
                            },
                            onDeleteExpense = { exp ->
                                SoundManager.playDelete(context = context)
                                viewModel.deleteExpense(exp)
                            }
                        )
                    }

                    AccountingTab.LEDGER -> {
                        LedgerScreen(
                            transactions = transactions,
                            currencySymbol = currencySymbol,
                            onSettleTransaction = { txId, customerName, amount ->
                                SoundManager.playCash(context = context)
                                viewModel.settleTransaction(txId, customerName, amount)
                                scope.launch {
                                    snackbarHostState.showSnackbar("تم تسديد الدين بنجاح")
                                }
                            },
                            onDeleteTransaction = { tx ->
                                SoundManager.playDelete(context = context)
                                viewModel.deleteTransaction(tx)
                            }
                        )
                    }

                    AccountingTab.SETTINGS -> {
                        SettingsScreen(
                            currentCurrency = currencySymbol,
                            auditLogs = auditLogs,
                            hasPreviousBackup = previousBackupJson != null,
                            onExportBackup = {
                                SoundManager.playClick(context = context)
                                scope.launch {
                                    backupJsonContent = viewModel.getExportJson()
                                }
                            },
                            onOpenRestoreDialog = {
                                SoundManager.playClick(context = context)
                                showRestoreDialog = true
                            },
                            onPickJsonFile = {
                                SoundManager.playClick(context = context)
                                openJsonLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            onRollbackPrevious = {
                                SoundManager.playClick(context = context)
                                viewModel.rollbackPreviousBackup { success ->
                                    scope.launch {
                                        if (success) {
                                            snackbarHostState.showSnackbar("تمت استعادة النسخة السابقة بنجاح (Rollback)")
                                        } else {
                                            snackbarHostState.showSnackbar("تعذر التراجع أو لا توجد نسخة سابقة")
                                        }
                                    }
                                }
                            },
                            onClearAuditLogs = {
                                SoundManager.playDelete(context = context)
                                viewModel.clearAuditLogs()
                            },
                            onClearAllData = {
                                SoundManager.playDelete(context = context)
                                viewModel.clearAllData {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("تم مسح وتصفير كافة المواد والمعاملات بنجاح")
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Dialogs
    saleItemTarget?.let { item ->
        RecordSaleDialog(
            item = item,
            currencySymbol = currencySymbol,
            onDismiss = { saleItemTarget = null },
            onConfirm = { quantity, isDebt, customerName, customerPhone, note ->
                viewModel.recordSale(item, quantity, isDebt, customerName, customerPhone, note) {
                    scope.launch {
                        val msg = if (isDebt) "تم تسجيل دين على $customerName" else "تم تسجيل بيع نقدي $quantity × ${item.name}"
                        snackbarHostState.showSnackbar(msg)
                    }
                }
                saleItemTarget = null
            }
        )
    }

    if (showManualDebtDialog) {
        RecordManualDebtDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showManualDebtDialog = false },
            onConfirm = { customerName, customerPhone, amount, title, note ->
                viewModel.recordManualDebt(customerName, customerPhone, amount, title, note) {
                    scope.launch {
                        snackbarHostState.showSnackbar("تم تسجيل دين مباشر على: $customerName")
                    }
                }
                showManualDebtDialog = false
            }
        )
    }

    if (showAddItemDialog) {
        AddItemDialog(
            initialItem = null,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, category, unitPrice, colorHex, sku, description ->
                viewModel.addItem(name, category, unitPrice, colorHex, sku, description) {
                    scope.launch {
                        snackbarHostState.showSnackbar("تمت إضافة $name إلى الكتالوج")
                    }
                }
                showAddItemDialog = false
            }
        )
    }

    itemToEdit?.let { item ->
        AddItemDialog(
            initialItem = item,
            onDismiss = { itemToEdit = null },
            onConfirm = { name, category, unitPrice, colorHex, sku, description ->
                viewModel.updateItem(
                    item.copy(
                        name = name,
                        category = category,
                        unitPrice = unitPrice,
                        colorHex = colorHex,
                        sku = sku,
                        description = description
                    )
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar("تم تحديث المادة $name بنجاح")
                    }
                }
                itemToEdit = null
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            currencySymbol = currencySymbol,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { title, category, amount, note ->
                viewModel.addExpense(title, category, amount, note) {
                    scope.launch {
                        snackbarHostState.showSnackbar("تم تسجيل سند صرف: $title")
                    }
                }
                showAddExpenseDialog = false
            }
        )
    }

    if (showProfitDetailsDialog) {
        ProfitDetailsDialog(
            calc = profitCalc,
            currencySymbol = currencySymbol,
            onDismiss = { showProfitDetailsDialog = false }
        )
    }

    selectedCustomerForDetails?.let { summary ->
        CustomerDebtDetailsDialog(
            summary = summary,
            currencySymbol = currencySymbol,
            onDismiss = { selectedCustomerForDetails = null },
            onSettleAll = {
                viewModel.settleCustomerDebts(summary.customerName)
                selectedCustomerForDetails = null
                scope.launch {
                    snackbarHostState.showSnackbar("تم تسديد كامل ذمة العميل: ${summary.customerName}")
                }
            },
            onSettleTx = { txId, amount ->
                viewModel.settleTransaction(txId, summary.customerName, amount)
                selectedCustomerForDetails = null
                scope.launch {
                    snackbarHostState.showSnackbar("تم تسديد الفاتورة")
                }
            }
        )
    }

    backupJsonContent?.let { json ->
        BackupDialog(
            jsonContent = json,
            onDownloadFile = {
                downloadAndSaveBackup(json)
            },
            onDismiss = { backupJsonContent = null }
        )
    }

    if (showRestoreDialog) {
        RestoreBackupDialog(
            onDismiss = { showRestoreDialog = false },
            onPickFile = {
                openJsonLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
            },
            onConfirmRestore = { jsonStr ->
                viewModel.restoreBackup(jsonStr) { success ->
                    scope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("تم استيراد واستعادة قاعدة البيانات بنجاح (وتم حفظ النسخة السابقة للتراجع)")
                            showRestoreDialog = false
                        } else {
                            snackbarHostState.showSnackbar("فشل الاستيراد: يرجى التأكد من صحة كود JSON")
                        }
                    }
                }
            }
        )
    }
}
