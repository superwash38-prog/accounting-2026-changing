package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaleTransactionEntity
import com.example.ui.AppUtils
import com.example.ui.theme.*

enum class LedgerFilter(val title: String) {
    ALL("كافة المعاملات"),
    CASH("مبيعات نقدية"),
    UNPAID_DEBTS("ديون غير مسددة"),
    SETTLED_DEBTS("ديون مسددة")
}

@Composable
fun LedgerScreen(
    transactions: List<SaleTransactionEntity>,
    currencySymbol: String,
    onSettleTransaction: (Long, String, Double) -> Unit,
    onDeleteTransaction: (SaleTransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(LedgerFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(transactions, selectedFilter, searchQuery) {
        transactions.filter { tx ->
            val matchFilter = when (selectedFilter) {
                LedgerFilter.ALL -> true
                LedgerFilter.CASH -> !tx.isDebt
                LedgerFilter.UNPAID_DEBTS -> tx.isDebt && !tx.isSettled
                LedgerFilter.SETTLED_DEBTS -> tx.isDebt && tx.isSettled
            }
            val matchSearch = searchQuery.isBlank() ||
                    tx.itemName.contains(searchQuery, ignoreCase = true) ||
                    tx.customerName.contains(searchQuery, ignoreCase = true) ||
                    tx.note.contains(searchQuery, ignoreCase = true)
            matchFilter && matchSearch
        }
    }

    val totalAmountFiltered = remember(filteredList) {
        filteredList.sumOf { it.totalAmount }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top summary
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("إجمالي الحركات المعروضة", color = Slate400, fontSize = 12.sp)
                    Text(
                        AppUtils.formatMoney(totalAmountFiltered, currencySymbol),
                        color = Emerald400,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate800
                ) {
                    Text(
                        "${filteredList.size} عملية",
                        color = Slate200,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث في السجل المالي والعملاء...", color = Slate500, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald500,
                unfocusedBorderColor = Slate800,
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate100
            ),
            singleLine = true
        )

        // Filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Slate900)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LedgerFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Emerald600 else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = filter }
                ) {
                    Text(
                        filter.title,
                        color = if (isSelected) Color.White else Slate400,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        // Transactions list
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد حركات مطابقة للفلتر المحدد", color = Slate500, fontSize = 14.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredList.forEach { tx ->
                    val isUnpaidDebt = tx.isDebt && !tx.isSettled
                    val isSettledDebt = tx.isDebt && tx.isSettled

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnpaidDebt) Rose500.copy(alpha = 0.3f) else Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            tx.itemName,
                                            color = Slate100,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (tx.quantity > 1) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Slate800
                                            ) {
                                                Text(
                                                    "×${tx.quantity}",
                                                    color = Slate300,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (tx.isDebt && tx.customerName.isNotBlank()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text("العميل: ${tx.customerName}", color = Rose400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            if (tx.customerPhone.isNotBlank()) {
                                                Text("(${tx.customerPhone})", color = Slate500, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    Text(
                                        AppUtils.formatDate(tx.timestamp),
                                        color = Slate500,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        AppUtils.formatMoney(tx.totalAmount, currencySymbol),
                                        color = if (isUnpaidDebt) Rose400 else Emerald400,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            isUnpaidDebt -> Rose900.copy(alpha = 0.5f)
                                            isSettledDebt -> Emerald900.copy(alpha = 0.5f)
                                            else -> Slate800
                                        },
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            when {
                                                isUnpaidDebt -> "دين معلق"
                                                isSettledDebt -> "دين مسدد"
                                                else -> "نقدي كاش"
                                            },
                                            color = when {
                                                isUnpaidDebt -> Rose400
                                                isSettledDebt -> Emerald300
                                                else -> Slate300
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (tx.note.isNotBlank()) {
                                Text(
                                    "ملاحظة: ${tx.note}",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }

                            // Footer actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onDeleteTransaction(tx) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Slate500, modifier = Modifier.size(15.dp))
                                }

                                if (isUnpaidDebt) {
                                    Button(
                                        onClick = { onSettleTransaction(tx.id, tx.customerName, tx.totalAmount) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تسديد الدين", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
