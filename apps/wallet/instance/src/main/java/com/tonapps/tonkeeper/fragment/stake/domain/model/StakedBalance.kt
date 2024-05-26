package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.os.Parcelable
import com.tonapps.tonkeeper.fragment.stake.domain.isAddressEqual
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.RateEntity
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class StakedBalance(
    val pool: StakingPool,
    val service: StakingService,
    val liquidBalance: StakedLiquidBalance?,
    val solidBalance: NominatorPool?,
    val fiatCurrency: WalletCurrency,
    val tonRate: RateEntity
) : Parcelable

@Parcelize
data class StakedLiquidBalance(
    val asset: DexAssetBalance,
    val assetRate: RateEntity,
) : Parcelable

fun StakedBalance.getAvailableCryptoBalance(): BigDecimal {
    return when {
        tonRate.value == BigDecimal.ZERO -> BigDecimal.ZERO
        else -> getAvailableFiatBalance() / tonRate.value
    }
}

fun StakedBalance.getAvailableFiatBalance(): BigDecimal {
    var total = BigDecimal.ZERO
    if (liquidBalance != null) {
        total += with(liquidBalance) { assetRate.value * asset.balance }
    }
    if (solidBalance != null) {
        total += solidBalance.amount * tonRate.value
    }
    return total
}

fun StakedBalance.getTotalFiatBalance(): BigDecimal {
    var total = BigDecimal.ZERO
    if (liquidBalance != null) {
        total += with(liquidBalance) { assetRate.value * asset.balance }
    }
    if (solidBalance != null) {
        total += with(solidBalance) {
            amount + pendingDeposit + pendingWithdraw + readyWithdraw
        } * tonRate.value
    }
    return total
}

fun StakedBalance.hasAddress(address: String): Boolean {
    return when {
        liquidBalance == null -> false
        else -> liquidBalance.asset.contractAddress.isAddressEqual(address)
    }
}

fun StakedBalance.hasPendingStake(): Boolean {
    return solidBalance?.pendingDeposit != null &&
            solidBalance.pendingDeposit.compareTo(BigDecimal.ZERO) == 1
}

fun StakedBalance.getPendingStakeBalance(): BigDecimal {
    return solidBalance?.pendingDeposit ?: BigDecimal.ZERO
}

fun StakedBalance.getPendingStakeBalanceFiat(): BigDecimal {
    return tonRate.value * getPendingStakeBalance()
}

fun StakedBalance.hasPendingUnstake(): Boolean {
    return solidBalance?.pendingWithdraw != null &&
            solidBalance.pendingWithdraw.compareTo(BigDecimal.ZERO) == 1
}

fun StakedBalance.getPendingUnstakeBalance(): BigDecimal {
    return solidBalance?.pendingWithdraw ?: BigDecimal.ZERO
}

fun StakedBalance.getPendingUnstakeBalanceFiat(): BigDecimal {
    return tonRate.value * getPendingUnstakeBalance()
}