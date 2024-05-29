package com.tonapps.tonkeeper.extensions

import android.content.Context
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.tonapps.tonkeeper.dialog.fiat.FiatDialog
import com.tonapps.tonkeeper.fragment.camera.CameraFragment
import com.tonapps.tonkeeper.fragment.send.SendScreen
import com.tonapps.tonkeeper.ui.screen.root.RootActivity
import com.tonapps.tonkeeper.ui.screen.swap.legacy.SwapScreenLegacy
import com.tonapps.tonkeeper.ui.screen.swap.main.SwapScreen
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.localization.Localization
import uikit.extensions.findFragment
import uikit.navigation.Navigation

fun Navigation.toast(@StringRes resId: Int, disableHaptic: Boolean = false) {
    val context = this as? Context ?: return
    toast(context.getString(resId), false, context.backgroundContentTintColor, disableHaptic)
}

fun Navigation.toast(message: String, @ColorInt color: Int, disableHaptic: Boolean = false) {
    toast(message, false, color, disableHaptic)
}

fun Navigation.toast(message: String, disableHaptic: Boolean = false) {
    val context = this as? Context ?: return
    toast(message, false, context.backgroundContentTintColor, disableHaptic)
}

fun Navigation.toastLoading(loading: Boolean, disableHaptic: Boolean = false) {
    val context = this as? Context ?: return
    toast(context.getString(Localization.loading), loading, context.backgroundContentTintColor, disableHaptic)
}

fun Navigation.openCamera() {
    add(CameraFragment.newInstance())
}

fun Navigation.sendCoin(
    address: String? = null,
    text: String? = null,
    amount: Float = 0f,
    jettonAddress: String? = null
) {
    if (this !is RootActivity) return

    val currentFragment = supportFragmentManager.findFragment<SendScreen>()
    if (currentFragment is SendScreen) {
        currentFragment.forceSetAddress(address)
        currentFragment.forceSetComment(text)
        currentFragment.forceSetAmount(amount)
        currentFragment.forceSetJetton(jettonAddress)
    } else {
        add(SendScreen.newInstance(address, text, amount, jettonAddress))
    }
}

// Contest

const val CONTEST_ENABLE_SWAP = true
const val CONTEST_ENABLE_PURCHASE = false
const val CONTEST_ENABLE_STAKE = false

fun Navigation.swapTokens(uri: Uri, address: String, walletType: WalletType, sendToken: TokenEntity, receiveToken: TokenEntity? = null, legacy: Boolean = !CONTEST_ENABLE_SWAP) {
    if (this is RootActivity) {
        // Task #1 (swap)
        if (legacy) {
            add(SwapScreenLegacy.newInstance(uri, address, sendToken.address))
        } else {
            add(SwapScreen.newInstance(uri, address, walletType, sendToken, receiveToken))
        }
    }
}

fun Navigation.buyCoins(address: String, walletType: WalletType, token: TokenEntity, legacy: Boolean = !CONTEST_ENABLE_PURCHASE) {
    if (this !is RootActivity) return

    if (legacy) {
        val context = this as? Context ?: return
        FiatDialog.open(context)
        return
    }

    // TODO: Task #2 (buy or sell)
}

fun Navigation.stakeCoins(address: String, walletType: WalletType, token: TokenEntity) {
    if (this !is RootActivity) return
    // TODO: Task #3 (stake)
}