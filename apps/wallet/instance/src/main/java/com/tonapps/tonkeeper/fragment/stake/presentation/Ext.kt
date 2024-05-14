package com.tonapps.tonkeeper.fragment.stake.presentation

import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingServiceType
import com.tonapps.tonkeeper.fragment.stake.root.StakeViewModel
import com.tonapps.wallet.localization.R
import java.math.BigDecimal

fun StakingServiceType.getIconUrl() = "res:/${getIconDrawableRes()}"

private fun StakingServiceType.getIconDrawableRes(): Int {
    return when (this) {
        StakingServiceType.TF -> com.tonapps.tonkeeperx.R.drawable.ic_staking_tf
        StakingServiceType.WHALES -> com.tonapps.tonkeeperx.R.drawable.ic_staking_whales
        StakingServiceType.LIQUID_TF -> com.tonapps.tonkeeperx.R.drawable.ic_staking_tonstakers
    }
}

fun StakingPool.description(): TextWrapper {
    val apy = CurrencyFormatter.formatFloat(
        apy.toFloat(),
        2
    ) // todo properly work with bigdecimals
    val minStaking = BigDecimal(minStake).movePointLeft(8)
    val minStakingString = CurrencyFormatter.format(StakeViewModel.TOKEN_TON, minStaking)
    return TextWrapper.StringResource(R.string.staking_pool_description_mask, apy, minStakingString)
}

fun StakingPool.apyText(): TextWrapper {
    val apy = CurrencyFormatter.formatFloat(
        apy.toFloat(),
        2
    )
    return TextWrapper.StringResource(R.string.apy_mask, apy)
}