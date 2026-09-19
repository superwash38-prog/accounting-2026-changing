package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuditLogEntity
import com.example.ui.AppUtils
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    currentCurrency: String,
    auditLogs: List<AuditLogEntity>,
    hasPreviousBackup: Boolean,
    onExportBackup: () -> Unit,
    onOpenRestoreDialog: () -> Unit,
    onPickJsonFile: () -> Unit = {},
    onRollbackPrevious: () -> Unit,
    onClearAuditLogs: () -> Unit,
    onClearAllData: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showConfirmClearDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App identity card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Emerald500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Emerald400, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text("بيت المحاسبة - Accounting House", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("نظام إدارة الحسابات ونقاط البيع والديون", color = Slate400, fontSize = 12.sp)
                    }
                }

                Divider(color = Slate800)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إصدار المنظومة:", color = Slate400, fontSize = 12.sp)
                    Text("v2000.4 (Android Edition)", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("العملة المعتمدة:", color = Slate400, fontSize = 12.sp)
                    Text(currentCurrency, color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("قاعدة البيانات المحلية:", color = Slate400, fontSize = 12.sp)
                    Text("Room SQLite Encrypted", color = Slate200, fontSize = 12.sp)
                }
            }
        }

        // Sound Effects & Haptics Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Emerald400, modifier = Modifier.size(22.dp))
                    Text("المؤثرات الصوتية والاهتزاز للأزرار", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Text(
                    "أصوات تفاعلية احترافية تصدر فور النقر على الأزرار (أصوات الكاشير، إتمام البيع، النقر، والحذف) لتأكيد العمليات بوضوح:",
                    color = Slate400,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                var soundEnabled by remember { mutableStateOf(com.example.ui.SoundManager.isSoundEnabled) }
                var hapticEnabled by remember { mutableStateOf(com.example.ui.SoundManager.isHapticEnabled) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = if (soundEnabled) Emerald400 else Slate500, modifier = Modifier.size(20.dp))
                        Text("تشغيل أصوات الأزرار (Sound Effects)", color = Slate200, fontSize = 13.sp)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            com.example.ui.SoundManager.isSoundEnabled = it
                            if (it) com.example.ui.SoundManager.playClick(context = context)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald400, checkedTrackColor = Emerald900)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = if (hapticEnabled) Sky400 else Slate500, modifier = Modifier.size(20.dp))
                        Text("الاهتزاز اللمسي الخفيف (Haptics)", color = Slate200, fontSize = 13.sp)
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = {
                            hapticEnabled = it
                            com.example.ui.SoundManager.isHapticEnabled = it
                            if (it) com.example.ui.SoundManager.playClick(context = context)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Sky400, checkedTrackColor = Sky900)
                    )
                }

                // Sound Test buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { com.example.ui.SoundManager.playClick(context = context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نقر", fontSize = 11.sp, color = Slate200)
                    }
                    OutlinedButton(
                        onClick = { com.example.ui.SoundManager.playCash(context = context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald700)
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("كاشير", fontSize = 11.sp, color = Emerald300)
                    }
                    OutlinedButton(
                        onClick = { com.example.ui.SoundManager.playSuccess(context = context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Sky700)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Sky400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نجاح", fontSize = 11.sp, color = Sky300)
                    }
                }
            }
        }

        // Backup, Export, Copy, and Redeem (Restore) Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Sky400, modifier = Modifier.size(22.dp))
                    Text("النسخ الاحتياطي واستعادة البيانات (JSON)", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Text(
                    "يمكنك تصدير وتنزيل ملف النسخة الاحتياطية، نسخه للحافظة، أو استرداد حزمة سابقة بسهولة مع حفظ نقطة تراجع للعودة إليها إذا أردت:",
                    color = Slate400,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Export / Download JSON button
                    Button(
                        onClick = onExportBackup,
                        colors = ButtonDefaults.buttonColors(containerColor = Sky500),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تنزيل ملف JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Redeem / Restore JSON button
                    Button(
                        onClick = onOpenRestoreDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استيراد واستعادة JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Direct instant redeem button from file picker
                OutlinedButton(
                    onClick = onPickJsonFile,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald400),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اختيار ملف JSON من الهاتف واسترداده تلقائياً (Redeem)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // If a previous backup snapshot exists before the last restore, give an instant rollback button!
                if (hasPreviousBackup) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Amber900.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Amber500.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = Amber400, modifier = Modifier.size(18.dp))
                                Text("توجد نقطة استعادة سابقة محفوظة قبل آخر استيراد", color = Amber300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                "إذا أردت التراجع عن البيانات المستوردة والعودة للنسخة السابقة التي كانت موجودة قبل الاستيراد:",
                                color = Slate300,
                                fontSize = 11.sp
                            )
                            Button(
                                onClick = onRollbackPrevious,
                                colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("العودة إلى النسخة السابقة (Rollback)", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Audit Trail Logs
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = Amber400, modifier = Modifier.size(20.dp))
                        Text("سجل التدقيق والرقابة (${auditLogs.size})", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    if (auditLogs.isNotEmpty()) {
                        TextButton(
                            onClick = onClearAuditLogs,
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("مسح السجل", color = Rose400, fontSize = 11.sp)
                        }
                    }
                }

                if (auditLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد سجلات تدقيق حتى الآن", color = Slate500, fontSize = 12.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        auditLogs.take(15).forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate850,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(log.title, color = Slate200, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text(AppUtils.formatDateShort(log.timestamp), color = Slate500, fontSize = 10.sp)
                                    }
                                    Text(log.details, color = Slate400, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Data Reset / Clean Database Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Rose400, modifier = Modifier.size(20.dp))
                    Text("إدارة وتهيئة البيانات", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Text(
                    "تفريغ كافة المواد والمعاملات والمصاريف للبدء ببيانات جديدة فارغة بالكامل:",
                    color = Slate400,
                    fontSize = 12.sp
                )

                OutlinedButton(
                    onClick = { showConfirmClearDataDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose400),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مسح كافة المواد والمعاملات (تصفير النظام)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    if (showConfirmClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDataDialog = false },
            title = {
                Text("تأكيد مسح البيانات", color = Slate100, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "هل أنت متأكد من رغبتك في تفريغ ومسح كافة المواد، المبيعات، الديون، والمصاريف؟ سيصبح النظام فارغاً تماماً وجاهزاً لإدخال بياناتك الجديدة.",
                    color = Slate300,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showConfirmClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("نعم، مسح وتصفير", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDataDialog = false }) {
                    Text("إلغاء", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}
