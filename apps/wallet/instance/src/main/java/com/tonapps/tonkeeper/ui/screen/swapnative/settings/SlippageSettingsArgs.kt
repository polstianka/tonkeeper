package com.tonapps.tonkeeper.ui.screen.swapnative.settings

import android.os.Bundle
import uikit.base.BaseArgs

data class SlippageSettingsArgs(
    val slippage: Float
) : BaseArgs() {

    companion object {
        private const val SLIPPAGE = "slippage"
    }

    constructor(bundle: Bundle) : this(
        slippage = bundle.getFloat(SLIPPAGE)
    )

    override fun toBundle() = Bundle().apply {
        putFloat(SLIPPAGE, slippage)
    }
}