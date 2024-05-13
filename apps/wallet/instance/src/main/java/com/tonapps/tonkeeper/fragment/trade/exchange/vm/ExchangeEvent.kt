package com.tonapps.tonkeeper.fragment.trade.exchange.vm

sealed class ExchangeEvent {
    data class NavigateToPickOperator(
        val paymentMethodId: String,
        val paymentMethodName: String,
        val country: String,
        val currencyCode: String,
        val amount: Float
    ) : ExchangeEvent()
}