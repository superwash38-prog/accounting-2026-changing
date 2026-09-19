package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerDebtSummary
import com.example.ui.AppUtils
import com.example.ui.theme.*

@Composable
fun DebtsScreen(
    customerDebts: List<CustomerDebtSummary>,
    currencySymbol: String,
    onRecordManualDebt: () -> Unit,
    onSelectCustomer: (CustomerDebtSummary) -> Unit,
    onSettleAllDebts: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalUnpaid = customerDebts.sumOf { it.totalUnpaidDebt }
    val unpaidCount = customerDebts.count { it.totalUnpaidDebt > 0 }

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
            border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.4f)),
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
                        Text("إجمالي ديون الزبائن المعلقة", color = Slate400, fontSize = 12.sp)
                        Text(
                            AppUtils.formatMoney(totalUnpaid, currencySymbol),
                            color = Rose400,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }

                    Button(
                        onClick = onRecordManualDebt,
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تسجيل دين مباشر", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "$unpaidCount عملاء بذمتهم مستحقات غير مسددة",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }
        }

        Text(
            "قائمة حسابات العملاء والذمم",
            color = Slate100,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        if (customerDebts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد ديون مسجلة على الزبائن", color = Slate500, fontSize = 14.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                customerDebts.forEach { summary ->
                    val hasUnpaid = summary.totalUnpaidDebt > 0
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (hasUnpaid) Rose500.copy(alpha = 0.3f) else Slate800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCustomer(summary) }
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        summary.customerName,
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (summary.customerPhone.isNotBlank()) {
                                        Text(summary.customerPhone, color = Slate400, fontSize = 12.sp)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        AppUtils.formatMoney(summary.totalUnpaidDebt, currencySymbol),
                                        color = if (hasUnpaid) Rose400 else Emerald400,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        if (hasUnpaid) "${summary.unpaidCount} فواتير غير مسددة" else "تم السداد بالكامل",
                                        color = if (hasUnpaid) Amber400 else Emerald500,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Actions: Settle All, Call, WhatsApp, View
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (summary.customerPhone.isNotBlank()) {
                                        FilledIconButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${summary.customerPhone}"))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {}
                                            },
                                            modifier = Modifier.size(34.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Slate850)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = Sky400, modifier = Modifier.size(16.dp))
                                        }

                                        FilledIconButton(
                                            onClick = {
                                                try {
                                                    val cleanPhone = summary.customerPhone.replace("[^0-9]".toRegex(), "")
                                                    val msg = "مرحباً ${summary.customerName}، نود تذكيركم بذمة مستحقة قدرها ${AppUtils.formatMoney(summary.totalUnpaidDebt, currencySymbol)} لدى بيت المحاسبة."
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(msg)}"))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {}
                                            },
                                            modifier = Modifier.size(34.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Slate850)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "واتساب", tint = Emerald400, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { onSelectCustomer(summary) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(34.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                                    ) {
                                        Text("كشف الحساب", fontSize = 11.sp)
                                    }
                                }

                                if (hasUnpaid) {
                                    Button(
                                        onClick = { onSettleAllDebts(summary.customerName) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تسديد الكل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
