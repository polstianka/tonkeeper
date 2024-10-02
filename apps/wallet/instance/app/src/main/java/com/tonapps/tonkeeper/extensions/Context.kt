package com.tonapps.tonkeeper.extensions

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.extensions.bestMessage
import com.tonapps.tonkeeperx.BuildConfig
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.data.account.Wallet
import com.tonapps.wallet.localization.Localization
import uikit.navigation.Navigation
import uikit.navigation.Navigation.Companion.navigation

fun Context.showToast(@StringRes resId: Int) {
    navigation?.toast(resId)
}

fun Context.loading(loading: Boolean = true) {
    navigation?.toastLoading(loading)
}

fun Context.result(isError: Boolean = false) {
    if (isError) {
        showToast(Localization.unknown_error)
    } else {
        showToast(Localization.done)
    }
}

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

fun Context.copyToClipboard(uri: Uri) {
    copyToClipboard(uri.toString())
}

fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = ClipData.newPlainText("", text)
    clipboard.setPrimaryClip(clip)
}

fun Context.hasPushPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
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

fun Context.buildRateString(rate: CharSequence, diff24h: String): CharSequence {
    if (diff24h.isEmpty() || diff24h == "0" || diff24h == "0.00%") {
        return SpannableString(rate)
    }
    val builder = SpannableStringBuilder()
    builder.append(rate)
    builder.append(" ")
    builder.append(diff24h)

    builder.setSpan(
        ForegroundColorSpan(getDiffColor(diff24h)),
        rate.length,
        rate.length + diff24h.length + 1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return builder
}

fun Context.getStringCompat(@StringRes resId: Int, vararg formatArgs: CharSequence?): CharSequence {
    return getString(resId).formatCompat(*formatArgs)
}

fun Context.getWalletBadges(
    type: Wallet.Type,
    version: WalletVersion
): CharSequence {
    var builder = SpannableStringBuilder()

    if (version == WalletVersion.V5R1 || version == WalletVersion.V5BETA) {
        val resId = if (version == WalletVersion.V5BETA) {
            Localization.w5beta
        } else {
            Localization.w5
        }
        builder = builder.badgeGreen(this, resId)
    }

    if (type != Wallet.Type.Default) {
        val resId = when (type) {
            Wallet.Type.Watch -> Localization.watch_only
            Wallet.Type.Testnet -> Localization.testnet
            Wallet.Type.Signer, Wallet.Type.SignerQR -> Localization.signer
            Wallet.Type.Ledger -> Localization.ledger
            Wallet.Type.Keystone -> Localization.keystone
            else -> throw IllegalArgumentException("Unknown wallet type: $type")
        }
        builder = builder.badgeDefault(this, resId)
    }

    return builder
}

fun Context.debugToast(e: Throwable) {
    debugToast(e.bestMessage)
}

fun Context.debugToast(text: String) {
    if (BuildConfig.DEBUG) {
        Navigation.from(this)?.toast(text)
    }
}