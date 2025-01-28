package com.tonapps.tonkeeper.extensions

import android.Manifest
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.extensions.bestMessage
import com.tonapps.tonkeeper.koin.settingsRepository
import com.tonapps.tonkeeper.manager.tonconnect.TonConnectManager
import com.tonapps.tonkeeperx.BuildConfig
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.data.account.Wallet
import com.tonapps.wallet.localization.Localization
import uikit.navigation.Navigation
import uikit.navigation.Navigation.Companion.navigation

val Context.workManager: WorkManager
    get() = WorkManager.getInstance(this)

val Context.uiMode: Int
    get() = resources.configuration.uiMode

val Context.isDarkMode: Boolean
    get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

val Context.isLightTheme: Boolean
    get() = settingsRepository?.theme?.light ?: false

fun Context.safeExternalOpenUri(uri: Uri) {
    if (TonConnectManager.isTonConnectDeepLink(uri)) {
        return
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Throwable) {
        debugToast(e)
    }
}

fun Context.showToast(@StringRes resId: Int) {
    navigation?.toast(resId)
}

fun Context.showToast(test: String) {
    navigation?.toast(test)
}

fun Context.loading(loading: Boolean = true) {
    navigation?.toastLoading(loading)
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

fun Context.copyToClipboard(text: String, sensitive: Boolean = false) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = ClipData.newPlainText("", text)
    if (sensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val extras = PersistableBundle()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        } else {
            extras.putBoolean("android.content.extra.IS_SENSITIVE", true)
        }
        clip.description.extras = extras
    }
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

fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.setData(Uri.fromParts("package", packageName, null))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}