package com.tonapps.tonkeeper.fragment.trade.buy.vm

sealed class BuyEvent {
    data class NavigateToPickOperator(
        val paymentMethodId: String,
        val paymentMethodName: String,
        val country: String,
        val currencyCode: String,
        val amount: Float
    ) : BuyEvent()
}