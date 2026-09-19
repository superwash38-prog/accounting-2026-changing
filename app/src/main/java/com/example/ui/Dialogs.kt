package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun RecordSaleDialog(
    item: ItemEntity,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, isDebt: Boolean, customerName: String, customerPhone: String, note: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var quantity by remember { mutableStateOf(1) }
    var isDebt by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val totalAmount = item.unitPrice * quantity

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تسجيل عملية بيع",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Slate400)
                    }
                }

                // Item info card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate850)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.name, color = Slate100, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(item.category, color = Slate400, fontSize = 12.sp)
                    }
                    Text(
                        AppUtils.formatMoney(item.unitPrice, currencySymbol),
                        color = Emerald400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الكمية المطلوبة:", color = Slate300, fontSize = 14.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledIconButton(
                            onClick = {
                                if (quantity > 1) {
                                    SoundManager.playClick(context = context)
                                    quantity--
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Slate800)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "نقص", tint = Slate100)
                        }
                        Text(
                            "$quantity",
                            color = Slate100,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                        FilledIconButton(
                            onClick = {
                                SoundManager.playClick(context = context)
                                quantity++
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Emerald600)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة", tint = Slate100)
                        }
                    }
                }

                // Payment Type Toggle (Cash / Debt)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate850)
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = {
                            SoundManager.playClick(context = context)
                            isDebt = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isDebt) Emerald600 else Color.Transparent,
                            contentColor = if (!isDebt) Color.White else Slate400
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("نقدي (فوري)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            SoundManager.playClick(context = context)
                            isDebt = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDebt) Rose500 else Color.Transparent,
                            contentColor = if (isDebt) Color.White else Slate400
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("دين (على الحساب)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // If Debt, show Customer info
                if (isDebt) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("اسم العميل (مطلوب للدين)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Rose400,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("رقم الهاتف (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Rose400,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        singleLine = true
                    )
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة أو تفاصيل (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    maxLines = 2
                )

                // Total calculation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDebt) Rose900.copy(alpha = 0.4f) else Emerald900.copy(alpha = 0.4f))
                        .border(1.dp, if (isDebt) Rose500.copy(alpha = 0.5f) else Emerald500.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isDebt) "المبلغ المسجل كدين:" else "المبلغ الإجمالي المستحق:", color = Slate300, fontSize = 13.sp)
                    Text(
                        AppUtils.formatMoney(totalAmount, currencySymbol),
                        color = if (isDebt) Rose400 else Emerald400,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                // Confirm button
                Button(
                    onClick = {
                        if (isDebt && customerName.isBlank()) return@Button
                        if (isDebt) {
                            SoundManager.playSuccess(context = context)
                        } else {
                            SoundManager.playCash(context = context)
                        }
                        onConfirm(quantity, isDebt, customerName, customerPhone, note)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDebt) Rose500 else Emerald600
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isDebt || customerName.isNotBlank()
                ) {
                    Text(
                        if (isDebt) "تأكيد تسجيل الدين" else "تأكيد واستلام النقد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RecordManualDebtDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (customerName: String, customerPhone: String, amount: Double, title: String, note: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تسجيل دين مباشر لعميل",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Rose400
                    )
                    IconButton(
                        onClick = {
                            SoundManager.playClick(context = context)
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Slate400)
                    }
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("اسم العميل / المدين *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Rose400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("مبلغ الدين ($currencySymbol) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Rose400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("السبب أو البيان (مثال: رصيد سابق، بضاعة)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate600,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("رقم الهاتف (اختياري للتواصل والواتساب)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate600,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate600,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    maxLines = 2
                )

                val validAmount = amountStr.toDoubleOrNull() ?: 0.0

                Button(
                    onClick = {
                        if (customerName.isNotBlank() && validAmount > 0) {
                            SoundManager.playSuccess(context = context)
                            onConfirm(customerName, customerPhone, validAmount, title, note)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                    shape = RoundedCornerShape(12.dp),
                    enabled = customerName.isNotBlank() && validAmount > 0
                ) {
                    Text("حفظ وتسجيل الدين في الذمة", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddItemDialog(
    initialItem: ItemEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, unitPrice: Double, colorHex: String, sku: String, description: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "خدمات غسيل") }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf(initialItem?.unitPrice?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var sku by remember { mutableStateOf(initialItem?.sku ?: "") }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var selectedColor by remember { mutableStateOf(initialItem?.colorHex ?: "#0EA5E9") }

    val presetColors = listOf("#0EA5E9", "#10B981", "#F59E0B", "#8B5CF6", "#F43F5E", "#06B6D4")
    val presetCategories = listOf(
        "خدمات غسيل",
        "تغيير زيت وصيانة",
        "مشروبات وضيافة",
        "مستلزمات وإكسسوارات",
        "تلميع وعناية",
        "خدمات أخرى",
        "+ تصنيف مخصص"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialItem != null) "تعديل المادة / الخدمة" else "إضافة مادة أو خدمة جديدة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Slate400)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المادة / الخدمة *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("سعر الوحدة *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )

                // Category chips
                Text("التصنيف أو النوع:", color = Slate400, fontSize = 12.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetCategories) { cat ->
                        val isSelected = if (cat == "+ تصنيف مخصص") isCustomCategory else (!isCustomCategory && category == cat)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Sky500 else Slate800,
                            modifier = Modifier.clickable {
                                if (cat == "+ تصنيف مخصص") {
                                    isCustomCategory = true
                                } else {
                                    isCustomCategory = false
                                    category = cat
                                }
                            }
                        ) {
                            Text(
                                cat,
                                color = if (isSelected) Color.White else Slate300,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                if (isCustomCategory) {
                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = { customCategoryText = it },
                        label = { Text("اكتب التصنيف المخصص (مثال: مشروبات باردة، فلاتر، تظليل)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Sky500,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("رمز SKU") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Slate600,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        singleLine = true
                    )

                    // Color picker row
                    Column(modifier = Modifier.weight(1f)) {
                        Text("اللون التعريفي:", color = Slate400, fontSize = 11.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            presetColors.take(4).forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(AppUtils.parseColor(hex))
                                        .border(
                                            if (selectedColor == hex) 2.dp else 0.dp,
                                            if (selectedColor == hex) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { selectedColor = hex }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate600,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    maxLines = 2
                )

                val validPrice = priceStr.toDoubleOrNull() ?: 0.0
                val finalCategory = if (isCustomCategory && customCategoryText.isNotBlank()) customCategoryText.trim() else category

                Button(
                    onClick = {
                        if (name.isNotBlank() && validPrice >= 0) {
                            SoundManager.playSuccess(context = context)
                            onConfirm(name, finalCategory, validPrice, selectedColor, sku, description)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank() && validPrice >= 0
                ) {
                    Text(
                        if (initialItem != null) "حفظ التعديلات" else "إضافة المادة للكتالوج",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double, note: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("مصاريف عامة") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val presetCategories = listOf("شراء زيوت وفلاتر", "مواد تنظيف وصابون", "شراء مشروبات وضيافة", "رواتب عمال", "إيجار ومرافق وماء", "صيانة معدات", "مصاريف عامة")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تسجيل مصروف جديد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber400
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Slate400)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("بيان المصروف (مثال: فاتورة كهرباء، صيانة) *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ المدفوع ($currencySymbol) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    singleLine = true
                )

                Text("التصنيف:", color = Slate400, fontSize = 12.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetCategories) { cat ->
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Amber500 else Slate800,
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                cat,
                                color = if (isSelected) Slate950 else Slate300,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة أو رقم الإيصال (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate600,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    maxLines = 2
                )

                val validAmount = amountStr.toDoubleOrNull() ?: 0.0

                Button(
                    onClick = {
                        if (title.isNotBlank() && validAmount > 0) {
                            SoundManager.playSuccess(context = context)
                            onConfirm(title, category, validAmount, note)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                    shape = RoundedCornerShape(12.dp),
                    enabled = title.isNotBlank() && validAmount > 0
                ) {
                    Text("حفظ وخصم المصروف", fontWeight = FontWeight.Bold, color = Slate950)
                }
            }
        }
    }
}

@Composable
fun ProfitDetailsDialog(
    calc: ProfitCalculation,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفاصيل معادلة الربح المحقق",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald400
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Slate400)
                    }
                }

                Text(
                    "يعتمد النظام على أساس الاستحقاق النقدي المحقق (Cash-Realized Basis) لضمان عدم توزيع أرباح وهمية من ديون لم تُحصّل بعد:",
                    fontSize = 12.sp,
                    color = Slate300,
                    lineHeight = 18.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate850)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1. إجمالي المقبوضات النقدية (+):", color = Slate300, fontSize = 13.sp)
                        Text(
                            AppUtils.formatMoney(calc.totalCashRevenue, currencySymbol),
                            color = Emerald400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("2. إجمالي المصاريف المدفوعة (-):", color = Slate300, fontSize = 13.sp)
                        Text(
                            "-${AppUtils.formatMoney(calc.totalExpenses, currencySymbol)}",
                            color = Rose400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Divider(color = Slate700)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("صافي الربح الفعلي في الصندوق:", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            AppUtils.formatMoney(calc.netCashProfit, currencySymbol),
                            color = if (calc.netCashProfit >= 0) Emerald300 else Rose400,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Rose900.copy(alpha = 0.3f))
                        .border(1.dp, Rose500.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "ديون معلقة (غير محصلة بعد):",
                        color = Rose400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        "${AppUtils.formatMoney(calc.totalUnpaidDebt, currencySymbol)} — تم استبعادها من صافي الربح وستُضاف تلقائياً عند قيام العميل بالتسديد.",
                        color = Slate300,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        lineHeight = 16.sp
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("فهمت ذلك", color = Slate100)
                }
            }
        }
    }
}

@Composable
fun CustomerDebtDetailsDialog(
    summary: CustomerDebtSummary,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSettleAll: () -> Unit,
    onSettleTx: (Long, Double) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = summary.customerName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        if (summary.customerPhone.isNotBlank()) {
                            Text(summary.customerPhone, color = Slate400, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Slate400)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Rose900.copy(alpha = 0.4f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مجموع الديون غير المسددة:", color = Slate300, fontSize = 13.sp)
                    Text(
                        AppUtils.formatMoney(summary.totalUnpaidDebt, currencySymbol),
                        color = Rose400,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                if (summary.totalUnpaidDebt > 0) {
                    Button(
                        onClick = {
                            SoundManager.playCash(context = context)
                            onSettleAll()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تسديد كامل الذمة دفعة واحدة", fontWeight = FontWeight.Bold)
                    }
                }

                Text("سجل عمليات هذا العميل:", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(summary.transactions) { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate850)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.itemName, color = Slate100, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    AppUtils.formatDateShort(tx.timestamp),
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    AppUtils.formatMoney(tx.totalAmount, currencySymbol),
                                    color = if (tx.isSettled) Emerald400 else Rose400,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (!tx.isSettled) {
                                    TextButton(
                                        onClick = {
                                            SoundManager.playCash(context = context)
                                            onSettleTx(tx.id, tx.totalAmount)
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("تسديد", color = Emerald400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("تم السداد", color = Emerald500, fontSize = 10.sp)
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
fun BackupDialog(
    jsonContent: String,
    onDownloadFile: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "النسخ الاحتياطي وحفظ البيانات",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Sky400
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Slate400)
                    }
                }

                Text(
                    "تم تجهيز حزمة النسخ الاحتياطي (JSON). يمكنك تنزيلها وحفظها في التنزيلات أو مشاركتها، أو نسخها مباشرة إلى الحافظة:",
                    fontSize = 12.sp,
                    color = Slate300,
                    lineHeight = 18.sp
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Slate950,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    LazyColumn(modifier = Modifier.padding(10.dp)) {
                        item {
                            Text(
                                jsonContent,
                                color = Emerald300,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }

                // Download file action
                Button(
                    onClick = {
                        SoundManager.playSuccess(context = context)
                        onDownloadFile()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Sky500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ وتنزيل كملف JSON", fontWeight = FontWeight.Bold)
                }

                // Copy to clipboard action
                OutlinedButton(
                    onClick = {
                        SoundManager.playClick(context = context)
                        clipboardManager.setText(AnnotatedString(jsonContent))
                        copied = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (copied) Emerald400 else Slate200
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (copied) Emerald400 else Slate700
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (copied) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (copied) "تم نسخ كود JSON إلى الحافظة بنجاح!" else "نسخ كود JSON إلى الحافظة", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RestoreBackupDialog(
    onDismiss: () -> Unit,
    onPickFile: () -> Unit,
    onConfirmRestore: (jsonText: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var jsonInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = Emerald400, modifier = Modifier.size(22.dp))
                        Text(
                            text = "استعادة حزمة البيانات (Redeem JSON)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Slate400)
                    }
                }

                Text(
                    "يمكنك اختيار ملف JSON مباشرة من هاتفك للاستعادة التلقائية، أو لصق كود JSON:",
                    fontSize = 12.sp,
                    color = Slate300,
                    lineHeight = 18.sp
                )

                // Pick file directly from device storage button
                Button(
                    onClick = {
                        SoundManager.playClick(context = context)
                        onPickFile()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اختيار ملف JSON من الهاتف واستعادته فوراً", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Slate700)
                    Text(" أو باللصق اليدوي ", color = Slate500, fontSize = 11.sp)
                    Divider(modifier = Modifier.weight(1f), color = Slate700)
                }

                // Quick Paste from Clipboard button
                OutlinedButton(
                    onClick = {
                        SoundManager.playClick(context = context)
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            jsonInput = clip
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Sky400, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("لصق سريع من الحافظة (Paste)", color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = {
                        jsonInput = it
                        errorMessage = null
                    },
                    placeholder = { Text("الصق بيانات JSON هنا...", color = Slate500, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate200,
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate950,
                        unfocusedContainerColor = Slate950
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                )

                errorMessage?.let { err ->
                    Text(err, color = Rose400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            SoundManager.playClick(context = context)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                    ) {
                        Text("إلغاء", color = Slate300)
                    }

                    Button(
                        onClick = {
                            if (jsonInput.isBlank()) {
                                errorMessage = "يرجى لصق بيانات JSON أولاً"
                            } else {
                                SoundManager.playSuccess(context = context)
                                onConfirmRestore(jsonInput.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تنفيذ الاستعادة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
