package com.tonapps.wallet.data.token.entities

import com.tonapps.wallet.data.core.WalletCurrency
import java.math.BigDecimal

data class TokenRateEntity(
    val currency: WalletCurrency,
    val fiat: BigDecimal,
    val rate: BigDecimal,
    val rateDiff24h: String
)