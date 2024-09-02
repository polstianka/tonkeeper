package com.tonapps.icu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ImageSpan
import android.util.ArrayMap
import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.ConcurrentHashMap

object CurrencyFormatter {

    private const val CURRENCY_SIGN = "¤"
    private const val SMALL_SPACE = " "
    private const val APOSTROPHE = "'"
    private const val TON_SYMBOL = "TON"

    private val fiatSymbols = ArrayMap<String, String>().apply {
        put("USD", "$")
        put("EUR", "€")
        put("RUB", "₽")
        put("AED", "د.إ")
        put("UAH", "₴")
        put("KZT", "₸")
        put("UZS", "UZS")
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
    }

    private val tokenSymbols = ArrayMap<String, String>().apply {
        put("BTC", "₿")
        put("ETH", "Ξ")
        put("USDT", "₮")
        put("USDC", "₵")
        put("DOGE", "Ð")
        put("TON", TON_SYMBOL)
    }

    private val symbols = fiatSymbols + tokenSymbols

    private val thresholds = listOf(
        0.0000000001 to 18,
        0.00000001 to 16,
        0.000001 to 8,
        0.0001 to 4,
        0.01 to 2
    )

    private val bigDecimalThresholds = listOf(
        BigDecimal("0.0000000001") to 18,
        BigDecimal("0.00000001") to 16,
        BigDecimal("0.000001") to 8,
        BigDecimal("0.0001") to 4,
        BigDecimal("0.01") to 2
    )

    private fun isFiat(currency: String): Boolean {
        return fiatSymbols.containsKey(currency)
    }

    private val format = NumberFormat.getCurrencyInstance() as DecimalFormat
    private val pattern = format.toPattern().replace(CURRENCY_SIGN, "").trim()
    private val cache = ConcurrentHashMap<String, DecimalFormat>(symbols.size, 1.0f, 2)

    val monetarySymbolFirstPosition = format.toPattern().startsWith(CURRENCY_SIGN)
    val monetaryDecimalSeparator = format.decimalFormatSymbols.monetaryDecimalSeparator.toString()

    fun format(
        currency: String = "",
        value: BigDecimal,
        customScale: Int = 0,
        roundingMode: RoundingMode = RoundingMode.DOWN,
        replaceSymbol: Boolean = true,
    ): CharSequence {
        val targetScale = getScale(value.abs())
        val scale = if (targetScale > customScale) targetScale else customScale
        val bigDecimal = value.stripTrailingZeros().setScale(scale, roundingMode).stripTrailingZeros()
        val decimals = bigDecimal.scale()
        val amount = getFormat(decimals).format(bigDecimal)
        return format(currency, amount, replaceSymbol)
    }

    fun format(
        currency: String = "",
        value: Coins,
        customScale: Int = 0,
        roundingMode: RoundingMode = RoundingMode.DOWN,
        replaceSymbol: Boolean = true,
    ): CharSequence {
        return format(currency, value.value, customScale, roundingMode, replaceSymbol)
    }

    fun formatFiat(
        currency: String,
        value: BigDecimal,
        customScale: Int = 2,
        roundingMode: RoundingMode = RoundingMode.DOWN,
        replaceSymbol: Boolean = true,
    ): CharSequence {
        return format(currency, value, customScale, roundingMode, replaceSymbol)
    }

    fun formatFiat(
        currency: String,
        value: Coins,
        customScale: Int = 2,
        roundingMode: RoundingMode = RoundingMode.DOWN,
        replaceSymbol: Boolean = true,
    ) = formatFiat(currency, value.value, customScale, roundingMode, replaceSymbol)

    private fun getScale(value: BigDecimal): Int {
        if (value == BigDecimal.ZERO) {
            return 0
        }
        return when {
            value >= BigDecimal.ONE -> 2
            value >= BigDecimal("0.1") -> 2
            value >= BigDecimal("0.01") -> 3
            else -> 4
        }
    }

    private fun getScale(value: Double): Int {
        if (value == 0.0) {
            return 0
        } else if (value <= 0.0) {
            return 2
        }

        for ((threshold, scale) in thresholds) {
            if (value < threshold) {
                return scale
            }
        }
        return 2
    }

    private fun format(
        currency: String = "",
        value: String,
        replaceSymbol: Boolean,
    ): CharSequence {
        val symbol = if (replaceSymbol) symbols[currency] else currency
        val builder = StringBuilder()
        if (symbol != null) {
            if (monetarySymbolFirstPosition && isFiat(currency)) {
                builder.append(symbol)
                builder.append(SMALL_SPACE)
                builder.append(value)
            } else {
                builder.append(value)
                builder.append(SMALL_SPACE)
                builder.append(symbol)
            }
        } else if (currency == "") {
            builder.append(value)
        } else {
            builder.append(value)
            builder.append(SMALL_SPACE)
            builder.append(currency)
        }
        return builder.toString()
    }

    fun CharSequence.withCustomSymbol(context: Context): CharSequence {
        if (true) { // Not now... maybe in future
            return this
        }
        val startIndex = indexOf(TON_SYMBOL)
        val endIndex = startIndex + TON_SYMBOL.length
        if (startIndex == -1) {
            return this
        }
        val previewChar = getOrNull(startIndex - 1) ?: ' '
        val nextChar = getOrNull(endIndex) ?: ' '
        if (previewChar.isLetter() || nextChar.isLetter()) {
            return this
        }

        val span = TONSymbolSpan(context)
        val spannableString = SpannableString(this)
        spannableString.setSpan(span, startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
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

    private open class CustomSymbolSpan(
        private val iconMedium: Drawable,
        private val iconBold: Drawable,
    ): ImageSpan(ColorDrawable(Color.TRANSPARENT), ALIGN_BOTTOM) {

        constructor(context: Context, mediumResId: Int, boldResId: Int) : this(
            context.getDrawable(mediumResId)!!,
            context.getDrawable(boldResId)!!
        )

        override fun getSize(
            paint: Paint,
            text: CharSequence?,
            start: Int,
            end: Int,
            fm: Paint.FontMetricsInt?
        ): Int {
            val drawable = createDrawable(paint) ?: return 0

            val bounds = drawable.bounds
            fm?.let { metrics ->
                val fontHeight = metrics.descent - metrics.ascent
                val drawableHeight = bounds.height()

                val centerY = metrics.ascent + fontHeight / 2

                metrics.ascent = centerY - drawableHeight / 2
                metrics.top = metrics.ascent
                metrics.descent = centerY + drawableHeight / 2
                metrics.bottom = metrics.descent
            }
            return bounds.right
        }

        private fun createDrawable(paint: Paint): Drawable? {
            if (paint is TextPaint) {
                return createDrawable(paint)
            }
            return null
        }

        private fun createDrawable(textPaint: TextPaint): Drawable {
            val isBold = textPaint.isFakeBoldText || textPaint.typeface.isBold
            val selectedDrawable = if (isBold) iconBold else iconMedium
            selectedDrawable.setTint(textPaint.color)
            setSize(selectedDrawable, textPaint.textSize)
            return selectedDrawable
        }

        open fun setSize(selectedDrawable: Drawable, size: Float) {
            val iconWidth = selectedDrawable.intrinsicWidth.toFloat()
            val iconHeight = selectedDrawable.intrinsicHeight.toFloat()
            val aspectRatio = iconWidth / iconHeight
            val width = (size * aspectRatio).toInt()
            selectedDrawable.setBounds(0, 0, width, size.toInt())
        }

        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            val drawable = createDrawable(paint) ?: return

            canvas.save()

            val fm = paint.fontMetricsInt
            val drawableHeight = drawable.bounds.height()
            val fontHeight = fm.descent - fm.ascent
            val centerY = y + fm.ascent + fontHeight / 2
            val transY = centerY - drawableHeight / 2
            canvas.translate(x, transY * 1.2f)
            drawable.draw(canvas)

            canvas.restore()
        }

    }

    private class TONSymbolSpan(context: Context): CustomSymbolSpan(context, R.drawable.ic_ton_symbol, R.drawable.ic_ton_bold_symbol) {

        override fun setSize(selectedDrawable: Drawable, size: Float) {
            super.setSize(selectedDrawable,size * 1.14f)
        }
    }
}