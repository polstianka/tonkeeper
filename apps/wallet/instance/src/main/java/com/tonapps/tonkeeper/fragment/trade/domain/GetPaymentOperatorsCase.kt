package com.tonapps.tonkeeper.fragment.trade.domain

import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import com.tonapps.tonkeeper.fragment.trade.domain.model.PaymentOperator

class GetPaymentOperatorsCase {
    suspend fun execute(
        country: String,
        paymentMethodId: String,
        currencyCode: String,
        direction: ExchangeDirection
    ): List<PaymentOperator> {
        val methods = when (direction) {
            ExchangeDirection.BUY -> App.fiat.getBuyMethods(country)
            ExchangeDirection.SELL -> App.fiat.getSellMethods(country)
        }
        return methods.map {
            PaymentOperator(
                id = it.id,
                name = it.title,
                description = it.description,
                iconUrl = it.iconUrl,
                url = it.actionButton.url,
                successUrlPattern = it.successUrlPattern,
                rate = "2.333 $currencyCode for 1 TON" // todo replace with real data
            )
        }
    }
}
