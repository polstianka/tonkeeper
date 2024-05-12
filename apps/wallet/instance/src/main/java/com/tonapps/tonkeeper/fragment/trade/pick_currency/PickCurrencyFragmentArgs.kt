package com.tonapps.tonkeeper.fragment.trade.pick_currency

import android.os.Bundle
import uikit.base.BaseArgs

class PickCurrencyFragmentArgs(
    val paymentMethodId: String,
    val pickedCurrencyCode: String?
) : BaseArgs() {

    companion object {
        private const val KEY_PAYMENT_METHOD_ID = "KEY_PAYMENT_METHOD_ID"
        private const val KEY_PICKED_CURRENCY_CODE = "KEY_PICKED_CURRENCY_CODE"
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putString(KEY_PAYMENT_METHOD_ID, paymentMethodId)
            pickedCurrencyCode?.let { putString(KEY_PICKED_CURRENCY_CODE, it) }
        }
    }

    constructor(bundle: Bundle) : this(
        bundle.getString(KEY_PAYMENT_METHOD_ID)!!,
        bundle.getString(KEY_PICKED_CURRENCY_CODE)
    )
}