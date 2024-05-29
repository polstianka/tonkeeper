package com.tonapps.tonkeeper.ui.screen.buyOrSell

import android.os.Bundle
import uikit.base.BaseArgs

data class BuyOrSellArgs(val address: String) : BaseArgs() {


    private companion object {
        private const val ARG_ADDRESS = "address"
    }
    constructor(bundle: Bundle) : this(
        address = bundle.getString(ARG_ADDRESS)!!,
    )
    override fun toBundle(): Bundle = Bundle().apply {
        putString(ARG_ADDRESS, address)
    }



}