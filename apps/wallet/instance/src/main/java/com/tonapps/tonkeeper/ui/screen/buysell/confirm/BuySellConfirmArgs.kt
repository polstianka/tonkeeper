package com.tonapps.tonkeeper.ui.screen.buysell.confirm

import android.os.Bundle
import com.tonapps.wallet.data.rates.entity.OperatorBuyRateEntity
import uikit.base.BaseArgs

data class BuySellConfirmArgs(
    val amount: Double,
    val operatorBuyRate: OperatorBuyRateEntity?,
    val fiatItem: String
) : BaseArgs() {

    companion object {
        private const val AMOUNT = "amount"
        private const val RATE = "rate"
        private const val FIAT_ITEM = "fiat_item"
    }

    constructor(bundle: Bundle) : this(
        amount = bundle.getDouble(AMOUNT),
        operatorBuyRate = bundle.getParcelable(RATE),
        fiatItem = bundle.getString(FIAT_ITEM, ""),
    )

    override fun toBundle() = Bundle().apply {
        putDouble(AMOUNT, amount)
        putParcelable(RATE, operatorBuyRate)
        putString(FIAT_ITEM, fiatItem)
    }
}