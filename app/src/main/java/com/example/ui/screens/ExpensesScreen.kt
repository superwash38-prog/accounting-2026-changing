package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.ExpenseEntity
import com.example.ui.AppUtils
import com.example.ui.theme.*

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    currencySymbol: String,
    onAddExpense: () -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = remember(expenses) {
        listOf("ALL") + expenses.map { it.category }.distinct()
    }

    val filteredExpenses = remember(expenses, selectedCategory) {
        if (selectedCategory == "ALL") expenses else expenses.filter { it.category == selectedCategory }
    }

    val totalExpenses = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.4f)),
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
                    Column {
                        Text("إجمالي المصاريف المسجلة", color = Slate400, fontSize = 12.sp)
                        Text(
                            AppUtils.formatMoney(totalExpenses, currencySymbol),
                            color = Amber400,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }

                    Button(
                        onClick = onAddExpense,
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مصروف", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate950)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "${filteredExpenses.size} سندات مصروفات مسجلة ومخصومة من الصندوق",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }
        }

        // Category filter chips
        if (categories.size > 2) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Amber500 else Slate850,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            if (cat == "ALL") "الكل (${expenses.size})" else cat,
                            color = if (isSelected) Slate950 else Slate400,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Expense items list
        if (filteredExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد مصاريف مسجلة", color = Slate500, fontSize = 14.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredExpenses.forEach { exp ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exp.title, color = Slate100, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Slate800
                                    ) {
                                        Text(exp.category, color = Amber400, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Text(AppUtils.formatDate(exp.timestamp), color = Slate500, fontSize = 11.sp)
                                }
                                if (exp.note.isNotBlank()) {
                                    Text(exp.note, color = Slate400, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "-${AppUtils.formatMoney(exp.amount, currencySymbol)}",
                                    color = Rose400,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                IconButton(
                                    onClick = { onDeleteExpense(exp) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Slate500, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
