package com.tonapps.tonkeeper.fragment.trade.domain.model

import java.math.BigDecimal

data class ExchangeMethod(
    val id: String,
    val name: String,
    val iconUrl: String,
    val minAmount: BigDecimal
)