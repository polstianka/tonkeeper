package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.os.Parcelable
import com.tonapps.tonkeeper.fragment.stake.domain.isAddressEqual
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.RateEntity
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class StakedBalance(
    val pool: StakingPool,
    val service: StakingService,
    val liquidBalance: StakedLiquidBalance?,
    val fiatCurrency: WalletCurrency,
    val tonRate: RateEntity
) : Parcelable

@Parcelize
data class StakedLiquidBalance(
    val asset: DexAsset,
    val assetRate: RateEntity,
) : Parcelable

fun StakedBalance.getFiatBalance(): BigDecimal {
    return when {
        liquidBalance == null -> BigDecimal.ZERO // todo
        else -> with(liquidBalance) { assetRate.value * asset.balance }
    }
}

fun StakedBalance.hasAddress(address: String): Boolean {
    return when {
        liquidBalance == null -> false // todo
        else -> liquidBalance.asset.contractAddress.isAddressEqual(address)
    }
}

fun StakedBalance.getCryptoBalance(): BigDecimal {
    return when {
        liquidBalance == null -> BigDecimal.ZERO // todo
        else -> getFiatBalance() / tonRate.value
    }
}