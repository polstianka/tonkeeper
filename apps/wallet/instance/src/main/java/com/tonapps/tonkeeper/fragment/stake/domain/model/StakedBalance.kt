package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.os.Parcelable
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class StakedBalance(
    val pool: StakingPool,
    val service: StakingService,
    val balance: BigDecimal,
    val asset: DexAsset?
) : Parcelable
