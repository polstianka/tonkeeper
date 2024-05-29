package com.tonapps.tonkeeper.extensions

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import uikit.extensions.withAlpha
import uikit.navigation.Navigation.Companion.navigation
import java.util.Locale

fun Context.copyWithToast(text: String, color: Int = backgroundContentTintColor) {
    navigation?.toast(getString(Localization.copied), color)
    copyToClipboard(text)
}

fun Context.clipboardText(): String {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = clipboard.primaryClip
    val text = clip?.getItemAt(0)?.text ?: ""
    return text.toString()
}

fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = ClipData.newPlainText("", text)
    clipboard.setPrimaryClip(clip)
}

fun Context.hasPushPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun Context.rateSpannable(rate: CharSequence, diff24h: String): SpannableString {
    val period = getString(Localization.period_24h)
    val span = SpannableString("$rate $diff24h $period")
    val color = getRateColor(diff24h)
    span.setSpan(
        ForegroundColorSpan(color),
        rate.length,
        span.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return span
}

fun Context.getSwapRateColor(priceImpact: Float): Int {
    return when {
        priceImpact < 0.01f -> textSecondaryColor
        priceImpact < 0.05f -> accentOrangeColor
        else -> accentRedColor
    }
}

@ColorInt
fun Context.getPriceImpactColor(priceImpact: Float): Int {
    return when {
        priceImpact < 0.01f -> accentGreenColor
        priceImpact < 0.05f -> accentOrangeColor
        else -> accentRedColor
    }
}

@ColorInt
fun Context.getDiffColor(diff: String): Int {
    return when {
        diff.startsWith("-") -> accentRedColor
        diff.startsWith("−") -> accentRedColor
        diff.startsWith("+") -> accentGreenColor
        else -> textSecondaryColor
    }
}

@ColorInt
private fun Context.getRateColor(diff: String): Int {
    return getDiffColor(diff).withAlpha(.64f)
}

fun Context.buildRateString(rate: CharSequence, diff24h: String): SpannableString {
    if (diff24h.isEmpty() || diff24h == "0") {
        return SpannableString(rate)
    }
    val span = SpannableString("$rate $diff24h")
    span.setSpan(
        ForegroundColorSpan(getDiffColor(diff24h)),
        rate.length,
        rate.length + diff24h.length + 1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return span
}
