package com.tonapps.tonkeeper.fragment.trade.pick_operator

import android.os.Bundle
import uikit.base.BaseArgs

class PickOperatorFragmentArgs(
    val paymentMethodId: String,
    val name: String,
    val country: String,
    val selectedCurrencyCode: String,
    val amount: Float
) : BaseArgs() {

    companion object {
        private const val KEY_ID = "KEY_ID"
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_COUNTRY = "KEY_COUNTRY"
        private const val KEY_SELECTED_CURRENCY_CODE = "KEY_SELECTED_CURRENCY_CODE"
        private const val KEY_AMOUNT = "KEY_AMOUNT "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putString(KEY_ID, paymentMethodId)
            putString(KEY_NAME, name)
            putString(KEY_COUNTRY, country)
            putString(KEY_SELECTED_CURRENCY_CODE, selectedCurrencyCode)
            putFloat(KEY_AMOUNT, amount)
        }
    }

    constructor(bundle: Bundle) : this(
        paymentMethodId = bundle.getString(KEY_ID)!!,
        name = bundle.getString(KEY_NAME)!!,
        country = bundle.getString(KEY_COUNTRY)!!,
        selectedCurrencyCode = bundle.getString(KEY_SELECTED_CURRENCY_CODE)!!,
        amount = bundle.getFloat(KEY_AMOUNT)
    )
}