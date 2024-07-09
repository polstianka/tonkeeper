package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class StakingService(
    val type: StakingServiceType,
    val pools: List<StakingPool>,
    val description: String,
    val name: String,
    val socials: List<StakingSocial>
) : Parcelable

/**
 * This only works because StakingService is only instantiated in map. And list of pools is sorted
 * during the construction. If any of these conditions change, this implementation won't work
 * */
val StakingService.maxAPY: BigDecimal
    get() = pools.first().apy