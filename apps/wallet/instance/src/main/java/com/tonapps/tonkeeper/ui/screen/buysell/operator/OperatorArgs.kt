package com.tonapps.tonkeeper.ui.screen.buysell.operator

import android.os.Bundle
import uikit.base.BaseArgs

data class OperatorArgs(
    val amount: Double
) : BaseArgs() {

    companion object {
        private const val AMOUNT = "amount"
    }

    constructor(bundle: Bundle) : this(
        amount = bundle.getDouble(AMOUNT),
    )

    override fun toBundle() = Bundle().apply {
        putDouble(AMOUNT, amount)
    }
}