package com.tonapps.tonkeeper.fragment.trade.domain

import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeMethod
import java.math.BigDecimal

class GetExchangeMethodsCase {

    companion object {
        private val minAmountBuy = BigDecimal("5.0")
        private val minAmountSell = BigDecimal("10.0")
    }

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
                        iconUrl = "",
                        minAmount = minAmountBuy
                    ),
                    ExchangeMethod(
                        id = "2",
                        name = "Cryptocurrency",
                        iconUrl = "",
                        minAmount = minAmountBuy
                    ),
                    ExchangeMethod(
                        id = "3",
                        name = "Google Pay",
                        iconUrl = "",
                        minAmount = minAmountBuy
                    )
                )
            }

            ExchangeDirection.SELL -> {
                listOf(
                    ExchangeMethod(
                        id = "1",
                        name = "Credit Card",
                        iconUrl = "",
                        minAmount = minAmountSell
                    ),
                    ExchangeMethod(
                        id = "2",
                        name = "Cryptocurrency",
                        iconUrl = "",
                        minAmount = minAmountSell
                    ),
                )
            }
        }
    }
}