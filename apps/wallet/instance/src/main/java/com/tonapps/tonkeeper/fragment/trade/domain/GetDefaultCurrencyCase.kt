package com.tonapps.tonkeeper.fragment.trade.domain

import com.tonapps.tonkeeper.fragment.trade.domain.model.AvailableCurrency

class GetDefaultCurrencyCase {

    // todo: replace with real data
    suspend fun execute(paymentMethodId: String): AvailableCurrency {
        return AvailableCurrency(
            "USD",
            "United States Dollar",
            paymentMethodId
        )
    }
}