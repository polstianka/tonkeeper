package com.tonapps.tonkeeper.fragment.trade.domain

import com.tonapps.tonkeeper.fragment.trade.domain.model.AvailableCurrency
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection

class GetDefaultCurrencyCase {

    // todo: replace with real data
    suspend fun execute(
        paymentMethodId: String,
        exchangeDirection: ExchangeDirection
    ): AvailableCurrency {
        return when (exchangeDirection) {
            ExchangeDirection.BUY -> {
                AvailableCurrency(
                    "USD",
                    "United States Dollar",
                    paymentMethodId
                )
            }
            ExchangeDirection.SELL -> {
                AvailableCurrency(
                    "EUR",
                    "Euro",
                    paymentMethodId
                )
            }
        }
    }
}