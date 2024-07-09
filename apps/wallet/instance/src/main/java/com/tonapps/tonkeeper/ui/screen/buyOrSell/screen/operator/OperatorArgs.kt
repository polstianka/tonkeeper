package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator

import android.os.Bundle
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.ui.screen.buyOrSell.BuyOrSellArgs
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist.CurrencyListArgs
import uikit.base.BaseArgs

data class OperatorArgs(
    val selectedTon: Double,
    val address: String
):  BaseArgs() {
    private companion object {
        private const val ARGS_SELECTED_TON = "KEY_SELECTED_TON"
        private const val ARG_ADDRESS = "address"
    }

    constructor(bundle: Bundle) : this(
        selectedTon = bundle.getDouble(ARGS_SELECTED_TON),
        address = bundle.getString(ARG_ADDRESS)!!,
        )

    override fun toBundle(): Bundle = Bundle().apply {
        putDouble(ARGS_SELECTED_TON, selectedTon)
        putString(ARG_ADDRESS, address)
    }

}