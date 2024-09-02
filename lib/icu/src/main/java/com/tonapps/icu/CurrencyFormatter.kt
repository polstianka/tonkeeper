package com.tonapps.icu

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.text.SpannableString
import android.util.Log
import com.tonapps.icu.format.CurrencyFormat
import com.tonapps.icu.format.TONSymbolSpan
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object CurrencyFormatter {

    private val customDigitLocales = listOf(
        "ar", "fa", "ur", "hi", "bn", "ta", "th", "lo", "my", "si"
    )

    private var format = CurrencyFormat(Locale.getDefault(Locale.Category.FORMAT))

    val monetaryDecimalSeparator: String
        get() = format.monetaryDecimalSeparator

    fun onConfigurationChanged(newConfig: Configuration) {
        val newLocale = newConfig.locales[0]
        onLocaleChanged(newLocale)
    }

    private fun onLocaleChanged(newLocale: Locale) {
        val newLanguage = newLocale.language
        if (newLanguage != "en" && customDigitLocales.contains(newLanguage)) {
            onLocaleChanged(Locale.US)
        } else if (newLanguage != format.locale.language) {
            format = CurrencyFormat(newLocale)
        }
    }

    fun format(
        currency: String = "",
        value: BigDecimal,
        customScale: Int = 0,
        roundingMode: RoundingMode = RoundingMode.DOWN,
        replaceSymbol: Boolean = true,
    ): CharSequence {
       return format.format(currency, value, customScale, roundingMode, replaceSymbol)
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
        val startIndex = indexOf(CurrencyFormat.TON_SYMBOL)
        val endIndex = startIndex + CurrencyFormat.TON_SYMBOL.length
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

}