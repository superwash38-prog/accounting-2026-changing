package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.ItemEntity
import com.example.data.ItemSalesSummary
import com.example.ui.AppUtils
import com.example.ui.theme.*

@Composable
fun ItemsScreen(
    items: List<ItemEntity>,
    summaries: List<ItemSalesSummary>,
    currencySymbol: String,
    onAddNewItem: () -> Unit,
    onEditItem: (ItemEntity) -> Unit,
    onDeleteItem: (ItemEntity) -> Unit,
    onSellItem: (ItemEntity) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "كتالوج المواد والخدمات",
                    color = Slate100,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "${items.size} مادة مسجلة في النظام",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onAddNewItem,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة مادة", fontWeight = FontWeight.Bold)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث بالاسم أو الرمز SKU...", color = Slate500, fontSize = 12.sp) },
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

        // Categories
        if (categories.size > 2) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Emerald600 else Slate850,
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

        // Items list
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (items.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Slate600, modifier = Modifier.size(42.dp))
                        Text("كتالوج المواد والخدمات فارغ", color = Slate300, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("اضغط على زر \"إضافة مادة\" بالأعلى لبدء تسجيل موادك وخدماتك الحقيقية", color = Slate500, fontSize = 12.sp)
                    }
                } else {
                    Text("لا توجد مواد مطابقة للبحث", color = Slate500, fontSize = 14.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredItems.forEach { item ->
                    val summary = summaries.firstOrNull { it.itemId == item.id }
                    val unitsSold = summary?.totalUnitsSold ?: 0
                    val rev = summary?.totalRevenue ?: 0.0
                    val color = AppUtils.parseColor(item.colorHex)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            item.name.take(1),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                    }
                                    Column {
                                        Text(item.name, color = Slate100, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(item.category, color = Slate400, fontSize = 11.sp)
                                            if (item.sku.isNotBlank()) {
                                                Text("#${item.sku}", color = Slate500, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }

                                Text(
                                    AppUtils.formatMoney(item.unitPrice, currencySymbol),
                                    color = Emerald400,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }

                            if (item.description.isNotBlank()) {
                                Text(item.description, color = Slate400, fontSize = 12.sp, maxLines = 2)
                            }

                            // Stats bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate850)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إجمالي المبيعات: $unitsSold وحدة", color = Slate300, fontSize = 11.sp)
                                Text(AppUtils.formatMoney(rev, currencySymbol), color = Emerald300, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { onEditItem(item) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Sky400, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteItem(item) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Rose400, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Button(
                                    onClick = { onSellItem(item) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تسجيل بيع", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
