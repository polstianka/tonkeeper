package com.tonapps.tonkeeper.fragment.trade.exchange.vm

import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import java.math.BigDecimal

sealed class ExchangeEvent {
    data class NavigateToPickOperator(
        val paymentMethodId: String,
        val paymentMethodName: String,
        val country: String,
        val currencyCode: String,
        val amount: BigDecimal,
        val direction: ExchangeDirection
    ) : ExchangeEvent()
}