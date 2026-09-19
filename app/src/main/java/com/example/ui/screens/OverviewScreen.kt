package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.AppUtils
import com.example.ui.components.SalesDonutChart
import com.example.ui.theme.*

@Composable
fun OverviewScreen(
    calc: ProfitCalculation,
    items: List<ItemEntity>,
    summaries: List<ItemSalesSummary>,
    currencySymbol: String,
    onShowProfitDetails: () -> Unit,
    onQuickSale: (ItemEntity) -> Unit,
    onOpenSaleDialog: (ItemEntity) -> Unit,
    onNavigateToTab: (AccountingTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = remember(items) {
        listOf("ALL") + items.map { it.category }.distinct()
    }

    val filteredItems = remember(items, searchQuery, selectedCategory) {
        items.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.sku.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == "ALL" || item.category == selectedCategory
            matchQuery && matchCat
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero: Realized Net Profit Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Emerald500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "صافي الربح الفعلي المحقق",
                                color = Slate300,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "المقبوضات النقدية مخصوماً منها المصاريف",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onShowProfitDetails,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald300),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الحساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = AppUtils.formatMoney(calc.netCashProfit, currencySymbol),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (calc.netCashProfit >= 0) Emerald400 else Rose400
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Slate800)
                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("المقبوض نقداً:", color = Slate400, fontSize = 11.sp)
                        Text(
                            AppUtils.formatMoney(calc.totalCashRevenue, currencySymbol),
                            color = Emerald300,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column {
                        Text("المصاريف المخصومة:", color = Slate400, fontSize = 11.sp)
                        Text(
                            "-${AppUtils.formatMoney(calc.totalExpenses, currencySymbol)}",
                            color = Rose400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column {
                        Text("ديون معلقة (غير محصلة):", color = Slate400, fontSize = 11.sp)
                        Text(
                            AppUtils.formatMoney(calc.totalUnpaidDebt, currencySymbol),
                            color = Amber400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Metrics Grid (2x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "إجمالي المبيعات",
                subtitle = "نقدي + ديون مسجلة",
                value = AppUtils.formatMoney(calc.totalAccrualRevenue, currencySymbol),
                icon = Icons.Default.Paid,
                iconTint = Emerald400,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "القطع والخدمات المباعة",
                subtitle = "معدل الطلب: ${AppUtils.formatMoney(calc.averageOrderValue, currencySymbol)}",
                value = "${calc.totalUnitsSold} قطعة",
                icon = Icons.Default.ShoppingBag,
                iconTint = Sky400,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "ديون الزبائن المعلقة",
                subtitle = "مستبعدة من صافي الربح",
                value = AppUtils.formatMoney(calc.totalUnpaidDebt, currencySymbol),
                icon = Icons.Default.CreditCard,
                iconTint = Rose400,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToTab(AccountingTab.DEBTS) }
            )
            MetricCard(
                title = "إجمالي المصاريف",
                subtitle = "تكاليف وتشغيل",
                value = AppUtils.formatMoney(calc.totalExpenses, currencySymbol),
                icon = Icons.Default.ReceiptLong,
                iconTint = Amber400,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToTab(AccountingTab.EXPENSES) }
            )
        }

        // Top Selling Item Banner (if any)
        if (calc.topItemName.isNotBlank() && calc.topItemRevenue > 0) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Amber500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Amber400, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("الخدمة / المادة الأكثر مبيعاً", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(calc.topItemName, color = Slate100, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(AppUtils.formatMoney(calc.topItemRevenue, currencySymbol), color = Amber300, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("إجمالي دخلها", color = Slate400, fontSize = 10.sp)
                    }
                }
            }
        }

        // Sales Breakdown Donut Chart
        SalesDonutChart(summaries = summaries, currencySymbol = currencySymbol)

        // Services & Quick Sales Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "المبيعات ونقاط البيع السريعة",
                    color = Slate100,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                TextButton(
                    onClick = { onNavigateToTab(AccountingTab.ITEMS) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("إدارة الكتالوج", color = Sky400, fontSize = 12.sp)
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Sky400, modifier = Modifier.size(16.dp))
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن خدمة أو منتج للبيع الفوري...", color = Slate500, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate800,
                    focusedContainerColor = Slate950,
                    unfocusedContainerColor = Slate950,
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate100
                ),
                singleLine = true
            )

            // Category Chips
            if (categories.size > 2) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Emerald600 else Slate800,
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                if (cat == "ALL") "الكل (${items.size})" else cat,
                                color = if (isSelected) Color.White else Slate400,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Items List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (items.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate600, modifier = Modifier.size(32.dp))
                            Text("لم يتم إضافة أي مواد أو خدمات بعد", color = Slate400, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { onNavigateToTab(AccountingTab.ITEMS) }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة مادة أو خدمة جديدة من تبويب المواد", color = Emerald400, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text("لا توجد مواد مطابقة للبحث", color = Slate500, fontSize = 12.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredItems.forEach { item ->
                        val summary = summaries.firstOrNull { it.itemId == item.id }
                        val unitsSold = summary?.totalUnitsSold ?: 0
                        val color = AppUtils.parseColor(item.colorHex)

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate950),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(color),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                item.name.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                        Column {
                                            Text(item.name, color = Slate100, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(item.category, color = Slate400, fontSize = 11.sp)
                                                if (item.sku.isNotBlank()) {
                                                    Text("#${item.sku}", color = Slate500, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            AppUtils.formatMoney(item.unitPrice, currencySymbol),
                                            color = Emerald400,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text("مباع $unitsSold مرة", color = Slate400, fontSize = 10.sp)
                                    }
                                }

                                // Action Buttons: Quick 1x Sale + Custom Sale
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onQuickSale(item) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Amber400, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("بيع فوري 1x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    OutlinedButton(
                                        onClick = { onOpenSaleDialog(item) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Sky400, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("بيع مخصص...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

@Composable
fun MetricCard(
    title: String,
    subtitle: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, color = Slate100, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = Slate500, fontSize = 10.sp, maxLines = 1)
        }
    }
}
