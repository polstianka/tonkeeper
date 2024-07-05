package com.tonapps.tonkeeper.ui.screen.swapnative.choose

import android.os.Bundle
import uikit.base.BaseArgs

data class ChooseTokenArgs(
    val sellContractAddress: String? = null,
) : BaseArgs() {

    companion object {
        private const val ARG_ADDRESS = "address"
    }

    constructor(bundle: Bundle) : this(
        sellContractAddress = bundle.getString(ARG_ADDRESS)
    )

    override fun toBundle() = Bundle().apply {
        putString(ARG_ADDRESS, sellContractAddress)
    }
}