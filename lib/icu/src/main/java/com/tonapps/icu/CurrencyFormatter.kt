package com.tonapps.icu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.ArrayMap
import android.util.Log
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.ConcurrentHashMap

object CurrencyFormatter {

    private const val CURRENCY_SIGN = "¤"
    private const val SMALL_SPACE = " "
    private const val APOSTROPHE = "'"
    private const val TON_SYMBOL = "TON"

    private val symbols = ArrayMap<String, String>().apply {
        put("USD", "$")
        put("EUR", "€")
        put("RUB", "₽")
        put("AED", "د.إ")
        put("UAH", "₴")
        put("KZT", "₸")
        put("UZS", "лв")
        put("GBP", "£")
        put("CHF", "₣")
        put("CNY", "¥")
        put("KRW", "₩")
        put("IDR", "Rp")
        put("INR", "₹")
        put("JPY", "¥")
        put("CAD", "C$")
        put("ARS", "ARS$")
        put("BYN", "Br")
        put("COP", "COL$")
        put("ETB", "ብር")
        put("ILS", "₪")
        put("KES", "KSh")
        put("NGN", "₦")
        put("UGX", "USh")
        put("VES", "Bs.\u200E")
        put("ZAR", "R")
        put("TRY", "₺")
        put("THB", "฿")
        put("VND", "₫")
        put("BRL", "R$")
        put("GEL", "₾")
        put("BDT", "৳")

        put("TON", TON_SYMBOL)
        put("BTC", "₿")
    }

    private fun isTON(currency: String): Boolean {
        return currency == "TON"
    }

    private fun isCrypto(currency: String): Boolean {
        return isTON(currency) || currency == "BTC"
    }

    private val format = NumberFormat.getCurrencyInstance() as DecimalFormat
    private val pattern = format.toPattern().replace(CURRENCY_SIGN, "").trim()
    private val cache = ConcurrentHashMap<String, DecimalFormat>(symbols.size, 1.0f, 2)

    val monetarySymbolFirstPosition = format.toPattern().startsWith(CURRENCY_SIGN)
    val monetaryDecimalSeparator = format.decimalFormatSymbols.monetaryDecimalSeparator.toString()

    fun formatFloat(
        value: Float,
        decimals: Int,
    ): String {
        if (0f >= value) {
            return "0"
        }
        return getFormat(decimals).format(value)
    }

    fun format(
        currency: String = "",
        value: Float,
        decimals: Int,
    ): CharSequence {
        val amount = formatFloat(value, decimals)
        return format(currency, amount)
    }

    fun format(
        currency: String = "",
        value: BigInteger,
        decimals: Int
    ): CharSequence {
        val amount = getFormat(decimals).format(value)
        return format(currency, amount)
    }

    fun format(
        currency: String = "",
        value: Float,
    ): CharSequence {
        val decimals = decimalCount(value)
        return format(currency, value, decimals)
    }

    fun format(
        currency: String = "",
        value: BigInteger
    ): CharSequence {
        var bigDecimal = value.toBigDecimal().stripTrailingZeros()
        if (bigDecimal.scale() > 0) {
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_DOWN)
        }
        val decimals = bigDecimal.scale()
        val amount = getFormat(decimals).format(value)
        return format(currency, amount)
    }

    fun format(
        currency: String = "",
        value: BigDecimal
    ): CharSequence {
        var bigDecimal = value.stripTrailingZeros()
        if (bigDecimal.scale() > 0) {
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_DOWN)
        }
        val decimals = bigDecimal.scale()
        val amount = getFormat(decimals).format(value)
        return format(currency, amount)
    }

    fun formatRate(
        currency: String,
        value: Float
    ): CharSequence {
        return format(currency, value, 4)
    }

    fun formatFiat(
        currency: String,
        value: Float
    ): CharSequence {
        var decimals = 2
        if (0.0001f > value) {
            decimals = 8
        } else if (0.01f > value) {
            decimals = 4
        }
        return format(currency, value, decimals)
    }

    private fun format(
        currency: String = "",
        value: String,
    ): CharSequence {
        val amount = removeTrailingZeros(value)
        val symbol = symbols[currency]
        val builder = StringBuilder()
        if (symbol != null) {
            if (monetarySymbolFirstPosition && !isCrypto(currency)) {
                builder.append(symbol)
                builder.append(SMALL_SPACE)
                builder.append(amount)
            } else {
                builder.append(amount)
                builder.append(SMALL_SPACE)
                builder.append(symbol)
            }
        } else if (currency == "") {
            builder.append(amount)
        } else {
            builder.append(amount)
            builder.append(SMALL_SPACE)
            builder.append(currency)
        }
        return builder.toString()
    }

    private fun decimalCount(value: Float): Int {
        if (0f >= value || value % 1.0f == 0.0f) {
            return 0
        }
        if (0.0001f > value) {
            return 8
        }
        if (0.01f > value) {
            return 4
        }
        return 2
    }

    private fun getFormat(decimals: Int): DecimalFormat {
        val key = cacheKey(decimals)
        var format = cache[key]
        if (format == null) {
            format = createFormat(decimals)
            cache[key] = format
        }
        return format
    }

    private fun cacheKey(decimals: Int): String {
        return decimals.toString()
    }

    private fun createFormat(decimals: Int): DecimalFormat {
        val decimalFormat = DecimalFormat(pattern)
        decimalFormat.maximumFractionDigits = decimals
        decimalFormat.minimumFractionDigits = decimals
        decimalFormat.groupingSize = 3
        decimalFormat.isGroupingUsed = true
        return decimalFormat
    }

    private fun removeTrailingZeros(value: String): String {
        if (true) {
            return value
        }
        if (!value.contains(monetaryDecimalSeparator)) {
            return value
        }
        val fixed = value.replace(Regex("${monetaryDecimalSeparator}?0+$"), "")
        if (fixed.endsWith(monetaryDecimalSeparator)) {
            return fixed.removeSuffix(monetaryDecimalSeparator)
        }
        return fixed
    }
}