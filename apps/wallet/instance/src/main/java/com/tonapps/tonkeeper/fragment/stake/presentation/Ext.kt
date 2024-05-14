package com.tonapps.tonkeeper.fragment.stake.presentation

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingServiceType

fun StakingServiceType.getIconUrl() = "res:/${getIconDrawableRes()}"

private fun StakingServiceType.getIconDrawableRes(): Int {
    return when (this) {
        StakingServiceType.TF -> com.tonapps.tonkeeperx.R.drawable.ic_staking_tf
        StakingServiceType.WHALES -> com.tonapps.tonkeeperx.R.drawable.ic_staking_whales
        StakingServiceType.LIQUID_TF -> com.tonapps.tonkeeperx.R.drawable.ic_staking_tonstakers
    }
}
