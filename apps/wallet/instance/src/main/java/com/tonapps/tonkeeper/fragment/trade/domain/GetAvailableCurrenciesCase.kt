package com.tonapps.tonkeeper.fragment.trade.domain

import com.tonapps.tonkeeper.fragment.trade.domain.model.AvailableCurrency

class GetAvailableCurrenciesCase {

    // todo: add caching
    // todo: replace with real data
    suspend fun execute(paymentMethodId: String): List<AvailableCurrency> {
        return listOf(
            AvailableCurrency(
                "USD",
                "United States Dollar",
                paymentMethodId
            ),
            AvailableCurrency(
                "EUR",
                "Euro",
                paymentMethodId
            ),
            AvailableCurrency(
                "RUB",
                "Russian Ruble",
                paymentMethodId
            ),
            AvailableCurrency(
                "AMD",
                "Armenian Dram",
                paymentMethodId
            ),
            AvailableCurrency(
                "GBP",
                "United Kingdom Pound",
                paymentMethodId
            ),
            AvailableCurrency(
                "CHF",
                "Swiss Franc",
                paymentMethodId
            ),
            AvailableCurrency(
                "CNY",
                "China Yuan",
                paymentMethodId
            ),
            AvailableCurrency(
                "KRW",
                "South Korea Won",
                paymentMethodId
            ),
            AvailableCurrency(
                "IDR",
                "Indonesian Rupiah",
                paymentMethodId
            ),
            AvailableCurrency(
                "INR",
                "Indian Rupee",
                paymentMethodId
            ),
            AvailableCurrency(
                "JPY",
                "Japanese Yen",
                paymentMethodId
            ),
        )
    }
}