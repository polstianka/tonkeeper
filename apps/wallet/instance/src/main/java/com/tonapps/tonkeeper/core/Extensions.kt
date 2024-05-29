package com.tonapps.tonkeeper.core

import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.tonapps.tonkeeper.App
import com.tonapps.uikit.color.resolveColor
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.animator.ArgbAnimator
import uikit.extensions.setPaddingBottom
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

fun ArgbAnimator.setTypedValue(context: Context, @AttrRes colorRef: Int, animated: Boolean = true) =
    changeValue(context.resolveColor(colorRef), animated)

val View.measuredWidthWithHorizontalMargins: Int
    get() = this.marginLeft + this.marginRight + this.measuredWidth
val View.measuredHeightWithVerticalMargins: Int
    get() = this.marginTop + this.marginBottom + this.measuredHeight

fun View.updateInsetPaddingBottom(offset: Int,
                                  progress: Float,
                                  isShowing: Boolean,
                                  navigationBarSize: Int,
                                  marginBottom: Int = 0) {
    val animating = progress != 0.0f && progress != 1.0f
    val paddingBottom = if (animating) {
        navigationBarSize + marginBottom
    } else {
        offset + marginBottom
    }
    if (this.paddingBottom != paddingBottom) {
        setPaddingBottom(paddingBottom)
    }
}

fun BigDecimal.scaleDownAndStripTrailingZeros(decimals: Int, roundingMode: RoundingMode = RoundingMode.DOWN): BigDecimal {
    val scaledDown = if (scale() > decimals) {
        setScale(decimals, roundingMode)
    } else {
        this
    }
    return scaledDown.stripTrailingZeros()
}

fun BigDecimal.toDefaultCoinAmount(token: TokenEntity? = null, format: NumberFormat? = App.defaultNumberFormat()): String {
    return this.toDisplayAmount(format, token?.decimals ?: -1)
}

fun BigDecimal.toDisplayAmount(format: NumberFormat? = null, maxFractionDigits: Int = -1, minFractionDigits: Int = -1): String {
    if (format != null) {
        try {
            if (format is DecimalFormat) {
                if (maxFractionDigits != -1) {
                    format.maximumFractionDigits = maxFractionDigits
                }
                if (minFractionDigits != -1) {
                    format.minimumFractionDigits = minOf(format.maximumFractionDigits, minFractionDigits)
                }
            }
            // NumberFormat.getNumberInstance(Locale.getDefault())
            return format.format(this)
        } catch (_: Exception) { }
    }
    return toPlainString()
}

fun String.modifyInputAmount(parsed: BigDecimal, numberFormat: NumberFormat? = null, maxFractionDigits: Int = -1, minFractionDigits: Int = -1): String =
    this.modifyInputAmount(parsed.toDisplayAmount(numberFormat, maxFractionDigits, minFractionDigits), numberFormat)

fun String.modifyInputAmount(parsed: String, numberFormat: NumberFormat? = null): String {
    if (this.isEmpty() || this == parsed) {
        return this
    }
    var groupingSeparator = ','
    var decimalSeparator = '.'
    if (numberFormat is DecimalFormat) {
        val decimalFormatSymbols = numberFormat.decimalFormatSymbols
        groupingSeparator= decimalFormatSymbols.groupingSeparator
        decimalSeparator = decimalFormatSymbols.decimalSeparator
    }

    if (this.length == 1) {
        when (this[0]) {
            decimalSeparator -> return "0$this"
            '.', ',' -> return "0$decimalSeparator"
            '0' -> return this
        }
    }

    var inputDigitCount = 0
    var inputDecimalSeparatorCount = 0
    var inputGroupingSeparatorCount = 0
    var inputEmpty = true

    for (char in this) {
        if (inputEmpty) {
            if (char == '0' || char == groupingSeparator) {
                continue
            }
            inputEmpty = false
        }
        when (char) {
            groupingSeparator -> inputGroupingSeparatorCount++
            decimalSeparator -> inputDecimalSeparatorCount++
            else -> inputDigitCount++
        }
    }
    val lastChar = this[this.length - 1]
    val endsWithDecimal = when (lastChar) {
        decimalSeparator -> inputDecimalSeparatorCount <= 1
        ',', '.' -> inputDecimalSeparatorCount == 0
        else -> false
    }
    if (endsWithDecimal) {
        return "$parsed$decimalSeparator"
    }
    if (inputDecimalSeparatorCount == 0 && inputDigitCount == 0) {
        return ""
    }

    return parsed
}

fun String?.prepareBigDecimal(numberFormat: NumberFormat? = null): String {
    if (isNullOrEmpty()) return "0"

    var groupingSeparator = ','
    var decimalSeparator = '.'

    if (numberFormat is DecimalFormat) {
        val decimalFormatSymbols = numberFormat.decimalFormatSymbols
        groupingSeparator= decimalFormatSymbols.groupingSeparator
        decimalSeparator = decimalFormatSymbols.decimalSeparator
    }

    var value = this.filterNot { it == groupingSeparator }
    var count = 0
    for (c in value) {
        if (c == groupingSeparator) {
            count++
        } else {
            break
        }
    }
    if (count > 0) {
        value = value.substring(count)
    }
    count = 0
    for (c in value) {
        if (c == decimalSeparator) {
            count++
        } else {
            break
        }
    }
    if (count > 0) {
        value = "0.${value.substring(count)}"
    }
    count = 0
    for (index in value.length - 1 downTo 0) {
        val char = value[index]
        if (char == decimalSeparator) {
            count++
        } else {
            break
        }
    }
    if (count > 0) {
        value = "${value.substring(0, value.length - count)}.0"
    }

    return value
}

fun String.parseBigDecimal(numberFormat: NumberFormat? = null, fallback: BigDecimal? = null): BigDecimal? {
    if (this.isEmpty()) {
        return fallback
    }
    val prepared = this.prepareBigDecimal(numberFormat)
    if (numberFormat is DecimalFormat) {
        numberFormat.isParseBigDecimal = true
        try {
            val number = numberFormat.parse(prepared)
            val bigDecimal = when (number) {
                is BigDecimal -> number
                is BigInteger -> number.toBigDecimal()
                is Long -> number.toBigDecimal()
                is Int -> number.toBigDecimal()
                is Float -> number.toBigDecimal()
                is Double -> number.toBigDecimal()
                else -> null
            }
            if (bigDecimal != null) {
                return bigDecimal
            }
        } catch (_: Exception) { }
    }
    try {
        return BigDecimal(prepared)
    } catch (_: Exception) { }
    return fallback
}

fun BigDecimal.toPercentage(decimals: Int = 2, roundingMode: RoundingMode = RoundingMode.DOWN): BigDecimal {
    val result = try {
        movePointRight(2)
    } catch (e: Exception) {
        this * BigDecimal.TEN.pow(2)
    }
    return if (decimals >= 0) {
        result.scaleDownAndStripTrailingZeros(decimals, roundingMode)
    } else {
        result.stripTrailingZeros()
    }
}