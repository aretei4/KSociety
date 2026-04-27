package com.khaga.ksociety.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtil {

    private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    /** Format as ₹10,000 */
    fun format(amount: Long): String =
        inrFormat.format(amount).replace(".00", "")

    /** Short format: ₹10K, ₹1L */
    fun formatShort(amount: Long): String = when {
        amount >= 100_000L -> "₹${amount / 100_000}L"
        amount >= 1_000L   -> "₹${amount / 1_000}K"
        else               -> "₹$amount"
    }
}
