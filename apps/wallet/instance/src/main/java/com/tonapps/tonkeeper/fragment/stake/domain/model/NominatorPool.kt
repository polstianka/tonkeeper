package com.tonapps.tonkeeper.fragment.stake.domain.model

import java.math.BigDecimal

data class NominatorPool(
    val stakingPool: StakingPool,
    val amount: BigDecimal,
    val pendingDeposit: BigDecimal,
    val pendingWithdraw: BigDecimal,
    val readyWithdraw: BigDecimal
)