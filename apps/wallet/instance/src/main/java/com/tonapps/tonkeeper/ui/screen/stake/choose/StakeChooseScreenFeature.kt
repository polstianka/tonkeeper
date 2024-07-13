package com.tonapps.tonkeeper.ui.screen.stake.choose

import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.ui.screen.stake.StakingRepository
import io.tonapi.models.PoolInfo
import uikit.mvi.UiFeature

class StakeChooseScreenFeature(
    val stakingRepository: StakingRepository
) : UiFeature<StakeChooseScreenState, StakeChooseScreenEffect>(StakeChooseScreenState()) {

    fun update(poolInfo: PoolInfo) {
        val impl = stakingRepository.implMap[poolInfo.implementation.value]
        val socials = mutableListOf<String>()
        impl?.url?.let { socials.add(it) }
        impl?.socials?.let { socials.addAll(it) }
        socials.add("https://tonviewer.com/${poolInfo.address}")
        updateUiState {
            it.copy(
                selectedPool = poolInfo,
                apy = "≈ ${CurrencyFormatter.format("%", poolInfo.apy)}",
                minDeposit = CurrencyFormatter.format("TON", Coin.toCoins(poolInfo.minStake)).toString(),
                socials = socials
            )
        }
    }
}