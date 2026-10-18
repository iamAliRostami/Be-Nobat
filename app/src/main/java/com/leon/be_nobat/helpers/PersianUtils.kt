package com.leon.be_nobat.helpers

import java.text.NumberFormat
import java.util.Locale

object PersianUtils {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        val chars = text.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (c in '0'..'9') {
                chars[i] = PERSIAN_DIGITS[c - '0']
            }
        }
        return String(chars)
    }

    fun toPersianDigits(number: Number?): String {
        if (number == null) return ""
        return toPersianDigits(number.toString())
    }

    fun formatNumber(number: Number?): String {
        if (number == null) return ""
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(number.toLong())
        return toPersianDigits(formatted)
    }

    fun formatPrice(amount: Number?, currency: String = "ریال"): String {
        if (amount == null) return ""
        val num = amount.toLong()
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(num)
        return "${toPersianDigits(formatted)} $currency"
    }

    fun formatDuration(minutes: Int): String {
        return "${toPersianDigits(minutes)} دقیقه"
    }
}

fun String?.toPersianDigits(): String = PersianUtils.toPersianDigits(this)
fun Number?.toPersianDigits(): String = PersianUtils.toPersianDigits(this)
fun Number?.formatPersianNumber(): String = PersianUtils.formatNumber(this)
fun Number?.formatPersianPrice(currency: String = "ریال"): String = PersianUtils.formatPrice(this, currency)
fun Int.formatPersianDuration(): String = PersianUtils.formatDuration(this)
