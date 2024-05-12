package com.tonapps.tonkeeper.fragment.trade.domain.model

import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern

data class PaymentOperator(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val url: String,
    val successUrlPattern: FiatSuccessUrlPattern?,
    val rate: String
)
