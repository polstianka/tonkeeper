package com.tonapps.tonkeeper.fragment.trade.domain

import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeMethod

class GetExchangeMethodsCase {

    // todo: fix when api is ready
    suspend fun execute(
        countryCode: String,
        exchangeDirection: ExchangeDirection
    ): List<ExchangeMethod> {
        return when (exchangeDirection) {
            ExchangeDirection.BUY -> {
                listOf(
                    ExchangeMethod(
                        id = "1",
                        name = "Credit Card",
                        iconUrl = ""
                    ),
                    ExchangeMethod(
                        id = "2",
                        name = "Cryptocurrency",
                        iconUrl = ""
                    ),
                    ExchangeMethod(
                        id = "3",
                        name = "Google Pay",
                        iconUrl = ""
                    )
                )
            }
            ExchangeDirection.SELL -> {
                listOf(
                    ExchangeMethod(
                        id = "1",
                        name = "Credit Card",
                        iconUrl = ""
                    ),
                    ExchangeMethod(
                        id = "2",
                        name = "Cryptocurrency",
                        iconUrl = ""
                    ),
                )
            }
        }
    }
}