package com.tonapps.tonkeeper.ui.screen.token

import android.os.Bundle
import uikit.base.BaseArgs

data class TokenArgs(
    val address: String,
    val name: String,
    val symbol: String,
    val staking: Boolean = false
): BaseArgs() {

    companion object {
        private const val ARG_ADDRESS = "address"
        private const val ARG_NAME = "name"
        private const val ARG_SYMBOL = "symbol"
        private const val ARG_STAKING = "staking"
    }

    constructor(bundle: Bundle) : this(
        address = bundle.getString(ARG_ADDRESS)!!,
        name = bundle.getString(ARG_NAME)!!,
        symbol = bundle.getString(ARG_SYMBOL)!!,
        staking = bundle.getBoolean(ARG_STAKING, false)
    )

    override fun toBundle() = Bundle().apply {
        putString(ARG_ADDRESS, address)
        putString(ARG_NAME, name)
        putString(ARG_SYMBOL, symbol)
        putBoolean(ARG_STAKING, staking)
    }
}