package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class NominatorPool(
    val stakingPool: StakingPool,
    val amount: BigDecimal,
    val pendingDeposit: BigDecimal,
    val pendingWithdraw: BigDecimal,
    val readyWithdraw: BigDecimal
) : Parcelable