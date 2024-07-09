package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class StakingPool(
    val address: String,
    val apy: BigDecimal,
    val currentNominators: Int,
    val cycleEnd: Long,
    val cycleLength: Long?,
    val cycleStart: Long,
    val serviceType: StakingServiceType,
    val liquidJettonMaster: String?,
    val maxNominators: Int,
    val minStake: BigDecimal,
    val name: String,
    val nominatorsStake: Long,
    val totalAmount: Long,
    val validatorStake: Long,
    val isMaxApy: Boolean
) : Parcelable