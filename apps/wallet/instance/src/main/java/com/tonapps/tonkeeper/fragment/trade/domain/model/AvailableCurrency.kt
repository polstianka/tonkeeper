package com.tonapps.tonkeeper.fragment.trade.domain.model

data class AvailableCurrency(
    val code: String,
    val name: String,
    val paymentMethodId: String
)