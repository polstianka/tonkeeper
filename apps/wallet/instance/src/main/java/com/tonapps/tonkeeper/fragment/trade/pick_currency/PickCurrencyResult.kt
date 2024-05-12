package com.tonapps.tonkeeper.fragment.trade.pick_currency

import android.os.Bundle
import uikit.base.BaseArgs

data class PickCurrencyResult(
    val currencyCode: String
) : BaseArgs() {

    companion object {
        private const val KEY_CURRENCY_CODE = "KEY_CURRENCY_CODE "
        const val KEY_REQUEST = "PickCurrencyRequest"
    }

    override fun toBundle(): Bundle {
        return Bundle().apply { putString(KEY_CURRENCY_CODE, currencyCode) }
    }

    constructor(bundle: Bundle) : this(bundle.getString(KEY_CURRENCY_CODE)!!)
}
