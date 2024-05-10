package com.tonapps.tonkeeper.fragment.trade.buy.vm

sealed class BuyEvent {
    data class PickOperator(
        val methodId: String,
        val methodName: String,
        val country: String
    ) : BuyEvent()
}