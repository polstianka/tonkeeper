package com.tonapps.tonkeeper.fragment.stake.presentation

import android.net.Uri
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingServiceType
import com.tonapps.wallet.localization.R

fun StakingServiceType.getIconUri() = "res:/${getIconDrawableRes()}"
    .let { Uri.parse(it) }

fun StakingServiceType.getIconDrawableRes(): Int {
    return when (this) {
        StakingServiceType.TF -> com.tonapps.tonkeeperx.R.drawable.ic_staking_tf
        StakingServiceType.WHALES -> com.tonapps.tonkeeperx.R.drawable.ic_staking_whales
        StakingServiceType.LIQUID_TF -> com.tonapps.tonkeeperx.R.drawable.ic_staking_tonstakers
    }
}

fun StakingPool.minStakingText(): String {
    return CurrencyFormatter.format("TON", minStake).toString()
}

fun StakingPool.description(): TextWrapper {
    val apy = formatApy()
    val minStakingString = minStakingText()
    return TextWrapper.StringResource(R.string.staking_pool_description_mask, apy, minStakingString)
}

fun StakingPool.apyText(): TextWrapper {
    val apy = formatApy()
    return TextWrapper.StringResource(R.string.apy_mask, apy)
}

fun StakingPool.formatApy(): String {
    return CurrencyFormatter.format(
        apy,
        2
    )
}