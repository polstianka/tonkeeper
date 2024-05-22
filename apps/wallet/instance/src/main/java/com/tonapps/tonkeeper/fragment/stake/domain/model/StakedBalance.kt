package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.os.Parcelable
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.RateEntity
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class StakedBalance(
    val pool: StakingPool,
    val service: StakingService,
    val balance: BigDecimal,
    val asset: DexAsset,
    val assetRate: RateEntity,
    val tonRate: RateEntity,
    val currency: WalletCurrency
) : Parcelable

fun StakedBalance.getFiatBalance(): BigDecimal {
    return assetRate.value * balance.movePointLeft(asset.decimals)
}

fun StakedBalance.getCryptoBalance(): BigDecimal {
    return getFiatBalance() / tonRate.value
}