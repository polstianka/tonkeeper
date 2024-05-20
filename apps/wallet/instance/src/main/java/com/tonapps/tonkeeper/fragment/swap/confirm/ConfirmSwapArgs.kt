package com.tonapps.tonkeeper.fragment.swap.confirm

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeper.fragment.swap.domain.model.toBundle
import com.tonapps.tonkeeper.fragment.swap.domain.model.toSwapSettings
import uikit.base.BaseArgs
import java.math.BigDecimal

data class ConfirmSwapArgs(
    val sendAsset: DexAsset,
    val receiveAsset: DexAsset,
    val settings: SwapSettings,
    val amount: BigDecimal
) : BaseArgs() {

    companion object {
        private const val KEY_SEND_ASSET = "KEY_SEND_ASSET "
        private const val KEY_RECEIVE_ASSET = "KEY_RECEIVE_ASSET"
        private const val KEY_SETTINGS = "KEY_SETTINGS"
        private const val KEY_AMOUNT = "KEY_AMOUNT"
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_SEND_ASSET, sendAsset)
            putParcelable(KEY_RECEIVE_ASSET, receiveAsset)
            putBundle(KEY_SETTINGS, settings.toBundle())
            putSerializable(KEY_AMOUNT, amount)
        }
    }

    constructor(bundle: Bundle) : this(
        sendAsset = bundle.getParcelable(KEY_SEND_ASSET)!!,
        receiveAsset = bundle.getParcelable(KEY_RECEIVE_ASSET)!!,
        settings = bundle.getBundle(KEY_SETTINGS)!!.toSwapSettings(),
        amount = bundle.getSerializable(KEY_AMOUNT) as BigDecimal
    )
}