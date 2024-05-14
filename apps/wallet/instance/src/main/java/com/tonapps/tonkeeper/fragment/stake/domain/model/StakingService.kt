package com.tonapps.tonkeeper.fragment.stake.domain.model

data class StakingService(
    val type: StakingServiceType,
    val pools: List<StakingPool>,
    val description: String,
    val name: String,
    val socials: List<StakingSocial>
)