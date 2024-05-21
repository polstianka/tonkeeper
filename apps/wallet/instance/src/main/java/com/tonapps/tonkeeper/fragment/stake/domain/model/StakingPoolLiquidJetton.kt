package com.tonapps.tonkeeper.fragment.stake.domain.model

import java.math.BigDecimal

data class StakingPoolLiquidJetton(
    val address: String,
    val iconUrl: String,
    val symbol: String,
    val price: BigDecimal,
    val poolName: String
)
