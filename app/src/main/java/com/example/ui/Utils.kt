package com.example.ui

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppUtils {
    val ChartColors = listOf(
        Emerald500,
        Sky500,
        Amber500,
        Purple500,
        Rose500,
        Cyan500
    )

    fun formatMoney(amount: Double, symbol: String = "ل.س"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        formatter.minimumFractionDigits = 0
        val formattedNumber = formatter.format(amount)
        return when (symbol) {
            "$" -> "$$formattedNumber"
            "€" -> "€$formattedNumber"
            "£" -> "£$formattedNumber"
            else -> "$formattedNumber $symbol"
        }
    }

    fun parseColor(hex: String, fallbackIndex: Int = 0): Color {
        return try {
            if (hex.startsWith("#") && (hex.length == 7 || hex.length == 9)) {
                val cleanHex = if (hex.length == 7) "#FF" + hex.substring(1) else hex
                Color(android.graphics.Color.parseColor(cleanHex))
            } else {
                ChartColors[fallbackIndex % ChartColors.size]
            }
        } catch (e: Exception) {
            ChartColors[fallbackIndex % ChartColors.size]
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM/dd hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
