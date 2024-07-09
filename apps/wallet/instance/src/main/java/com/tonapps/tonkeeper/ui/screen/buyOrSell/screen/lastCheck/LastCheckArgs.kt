package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.lastCheck

import android.os.Bundle
import uikit.base.BaseArgs

data class LastCheckArgs(
    val courseRate: Double,
    val sendAmount: Double,
    val sendCurrencyNm: String,
    val receiveCurrencyNm: String,
    val countryNm: String,
    val address: String
) : BaseArgs() {

    private companion object {
        private const val ARGS_KEY_COURSE_RATE = "KEY_COURSE_RATE"
        private const val ARGS_KEY_SEND_AMOUNT = "KET_SEND_AMOUNT"
        private const val ARGS_KEY_SEND_CURRENCY_NM = "KEY_SEND_CURRENCY_NM"
        private const val ARGS_KEY_RECEIVE_CURRENCY_NM = "KEY_RECEIVE_CURRENCY_NM"
        private const val ARGS_KEY_COUNTRY = "KEY_COUNTRY"
        private const val ARG_ADDRESS = "address"
    }

    constructor(bundle: Bundle) : this(
        courseRate = bundle.getDouble(ARGS_KEY_COURSE_RATE),
        sendAmount = bundle.getDouble(ARGS_KEY_SEND_AMOUNT),
        sendCurrencyNm = bundle.getString(ARGS_KEY_SEND_CURRENCY_NM).toString(),
        receiveCurrencyNm = bundle.getString(ARGS_KEY_RECEIVE_CURRENCY_NM).toString(),
        countryNm = bundle.getString(ARGS_KEY_COUNTRY).toString(),
        address = bundle.getString(ARG_ADDRESS)!!,
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putDouble(ARGS_KEY_COURSE_RATE, courseRate)
        putDouble(ARGS_KEY_SEND_AMOUNT, sendAmount)
        putString(ARGS_KEY_SEND_CURRENCY_NM, sendCurrencyNm)
        putString(ARGS_KEY_RECEIVE_CURRENCY_NM, receiveCurrencyNm)
        putString(ARGS_KEY_COUNTRY, countryNm)
        putString(ARG_ADDRESS, address)
    }

}