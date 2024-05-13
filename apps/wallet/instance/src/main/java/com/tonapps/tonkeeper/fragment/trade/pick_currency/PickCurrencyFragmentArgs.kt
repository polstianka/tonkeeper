package com.tonapps.tonkeeper.fragment.trade.pick_currency

import android.os.Bundle
import com.tonapps.extensions.getEnum
import com.tonapps.extensions.putEnum
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import uikit.base.BaseArgs

class PickCurrencyFragmentArgs(
    val paymentMethodId: String,
    val pickedCurrencyCode: String,
    val direction: ExchangeDirection
) : BaseArgs() {

    companion object {
        private const val KEY_PAYMENT_METHOD_ID = "KEY_PAYMENT_METHOD_ID"
        private const val KEY_PICKED_CURRENCY_CODE = "KEY_PICKED_CURRENCY_CODE"
        private const val KEY_EXCHANGE_DIRECTION = "KEY_EXCHANGE_DIRECTION"
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putString(KEY_PAYMENT_METHOD_ID, paymentMethodId)
            putString(KEY_PICKED_CURRENCY_CODE, pickedCurrencyCode)
            putEnum(KEY_EXCHANGE_DIRECTION, direction)
        }
    }

    constructor(bundle: Bundle) : this(
        bundle.getString(KEY_PAYMENT_METHOD_ID)!!,
        bundle.getString(KEY_PICKED_CURRENCY_CODE)!!,
        bundle.getEnum(KEY_EXCHANGE_DIRECTION, ExchangeDirection.BUY)
    )
}