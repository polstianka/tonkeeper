package com.tonapps.tonkeeper.fragment.trade.pick_operator

import android.os.Bundle
import com.tonapps.extensions.getEnum
import com.tonapps.extensions.putEnum
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import uikit.base.BaseArgs
import java.math.BigDecimal

class PickOperatorFragmentArgs(
    val exchangeDirection: ExchangeDirection,
    val paymentMethodId: String,
    val name: String,
    val country: String,
    val selectedCurrencyCode: String,
    val amount: BigDecimal
) : BaseArgs() {

    companion object {
        private const val KEY_ID = "KEY_ID"
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_COUNTRY = "KEY_COUNTRY"
        private const val KEY_SELECTED_CURRENCY_CODE = "KEY_SELECTED_CURRENCY_CODE"
        private const val KEY_AMOUNT = "KEY_AMOUNT "
        private const val KEY_EXCHANGE_DIRECTION = "KEY_EXCHANGE_DIRECTION "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putString(KEY_ID, paymentMethodId)
            putString(KEY_NAME, name)
            putString(KEY_COUNTRY, country)
            putString(KEY_SELECTED_CURRENCY_CODE, selectedCurrencyCode)
            putSerializable(KEY_AMOUNT, amount)
            putEnum(KEY_EXCHANGE_DIRECTION, exchangeDirection)
        }
    }

    constructor(bundle: Bundle) : this(
        exchangeDirection = bundle.getEnum(KEY_EXCHANGE_DIRECTION, ExchangeDirection.BUY),
        paymentMethodId = bundle.getString(KEY_ID)!!,
        name = bundle.getString(KEY_NAME)!!,
        country = bundle.getString(KEY_COUNTRY)!!,
        selectedCurrencyCode = bundle.getString(KEY_SELECTED_CURRENCY_CODE)!!,
        amount = bundle.getSerializable(KEY_AMOUNT) as BigDecimal
    )
}