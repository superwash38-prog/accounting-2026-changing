package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ItemSalesSummary
import com.example.ui.AppUtils
import com.example.ui.theme.*

@Composable
fun SalesDonutChart(
    summaries: List<ItemSalesSummary>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val totalRevenue = summaries.sumOf { it.totalRevenue }
    val activeSummaries = summaries.filter { it.totalRevenue > 0 }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "مخطط مساهمة المبيعات",
                color = Slate100,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                if (totalRevenue > 0) "${activeSummaries.size} مواد نشطة" else "لا توجد مبيعات",
                color = Slate400,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (totalRevenue <= 0) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Slate850)
                    .border(2.dp, Slate800, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد مبيعات بعد", color = Slate500, fontSize = 12.sp)
            }
        } else {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(170.dp)) {
                    val strokeWidth = 26.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    var startAngle = -90f

                    activeSummaries.forEach { item ->
                        val sweepAngle = (item.totalRevenue / totalRevenue * 360f).toFloat()
                        val color = AppUtils.parseColor(item.colorHex)

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 1.5f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }

                // Center label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجمالي المبيعات", color = Slate400, fontSize = 11.sp)
                    Text(
                        AppUtils.formatMoney(totalRevenue, currencySymbol),
                        color = Emerald400,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Breakdown legend list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeSummaries.take(5).forEach { item ->
                    val color = AppUtils.parseColor(item.colorHex)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate850.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(
                                item.itemName,
                                color = Slate200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${String.format(java.util.Locale.US, "%.1f", item.percentageOfTotal)}%",
                                color = Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                AppUtils.formatMoney(item.totalRevenue, currencySymbol),
                                color = Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
