package com.tonapps.tonkeeper.fragment.trade.pick_currency

sealed class PickCurrencyEvent {
    data class ReturnWithResult(val currencyCode: String): PickCurrencyEvent()
}