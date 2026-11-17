package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

const val CURRENCY_SYMBOL = "Rp"

private val indonesianSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}

private val integerFormat = DecimalFormat("#,##0", indonesianSymbols)

fun formatRupiah(amountIdr: Long, withSpace: Boolean = true): String {
    val absFormatted = integerFormat.format(kotlin.math.abs(amountIdr))
    val prefix = if (amountIdr < 0) "-" else ""
    val space = if (withSpace) " " else ""
    return "$prefix$CURRENCY_SYMBOL$space$absFormatted"
}

fun formatSignedRupiah(amountIdr: Long): String {
    val absFormatted = integerFormat.format(kotlin.math.abs(amountIdr))
    val sign = if (amountIdr >= 0) "+" else "-"
    return "$sign$CURRENCY_SYMBOL $absFormatted"
}

fun formatCompactRupiah(amountIdr: Long): String {
    val abs = kotlin.math.abs(amountIdr)
    val sign = if (amountIdr < 0) "-" else ""
    return when {
        abs >= 1_000_000_000L -> {
            val valM = abs / 1_000_000_000.0
            "$sign$CURRENCY_SYMBOL ${String.format(Locale("id", "ID"), "%.1fM", valM)}"
        }
        abs >= 1_000_000L -> {
            val valJt = abs / 1_000_000.0
            val formatted = if (abs % 1_000_000L == 0L) {
                (abs / 1_000_000L).toString()
            } else {
                String.format(Locale("id", "ID"), "%.1f", valJt)
            }
            "$sign$CURRENCY_SYMBOL ${formatted}jt"
        }
        abs >= 1_000L -> {
            "$sign$CURRENCY_SYMBOL ${abs / 1_000L}rb"
        }
        else -> "$sign$CURRENCY_SYMBOL $abs"
    }
}

fun formatKeypadInputRupiah(rawDigits: String): String {
    val clean = rawDigits.filter { it.isDigit() }
    if (clean.isEmpty()) return "0"
    val value = clean.toLongOrNull() ?: 0L
    return integerFormat.format(value)
}

fun formatCryptoUnits(units: Double, symbol: String): String {
    return "${String.format(Locale.US, "%.4f", units)} $symbol"
}
