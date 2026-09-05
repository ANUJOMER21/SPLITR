package com.omer.expensetracker.presentation.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

private const val MINOR_UNITS_PER_MAJOR = 100L
private val INDIA_LOCALE = Locale("en", "IN")

/** Formats minor units (paise) as an INR currency string with Indian digit grouping,
 * e.g. 12345678 -> "₹1,23,456.78". The app is INR-only by design — no other currency. */
fun Long.formatAsCurrency(): String {
    val major = BigDecimal(this).divide(BigDecimal(MINOR_UNITS_PER_MAJOR))
    return NumberFormat.getCurrencyInstance(INDIA_LOCALE).format(major)
}

/** Plain decimal amount (no currency symbol) suitable for an editable amount field, e.g. "1234.56". */
fun Long.toAmountInputString(): String =
    BigDecimal(this).divide(BigDecimal(MINOR_UNITS_PER_MAJOR)).setScale(2, RoundingMode.HALF_UP).toPlainString()

/** Parses a user-typed decimal amount string into minor units, or null if it isn't a valid positive number. */
fun String.parseAmountToMinorUnits(): Long? {
    val normalized = trim()
    if (normalized.isEmpty()) return null
    val decimal = normalized.toBigDecimalOrNull() ?: return null
    if (decimal.signum() <= 0) return null
    return decimal.multiply(BigDecimal(MINOR_UNITS_PER_MAJOR)).setScale(0, RoundingMode.HALF_UP).longValueExact()
}

private fun String.toBigDecimalOrNull(): BigDecimal? = try {
    BigDecimal(this)
} catch (e: NumberFormatException) {
    null
}
