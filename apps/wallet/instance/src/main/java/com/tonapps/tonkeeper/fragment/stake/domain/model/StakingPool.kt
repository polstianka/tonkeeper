package com.tonapps.tonkeeper.fragment.stake.domain.model

import java.math.BigDecimal

data class StakingPool(
    val address: String,
    val apy: BigDecimal,
    val currentNominators: Int,
    val cycleEnd: Long,
    val cycleLength: Long,
    val cycleStart: Long,
    val serviceType: StakingServiceType,
    val liquidJettonMaster: String,
    val maxNominators: Int,
    val minStake: Long,
    val name: String,
    val nominatorsStake: Long,
    val totalAmount: Long,
    val validatorStake: Long
)