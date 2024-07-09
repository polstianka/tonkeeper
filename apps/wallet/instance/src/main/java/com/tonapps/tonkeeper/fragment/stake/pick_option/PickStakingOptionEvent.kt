package com.tonapps.tonkeeper.fragment.stake.pick_option

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.wallet.data.core.WalletCurrency

sealed class PickStakingOptionEvent {
    object NavigateBack : PickStakingOptionEvent()
    object CloseFlow : PickStakingOptionEvent()
    data class ShowPoolPicker(
        val service: StakingService,
        val pickedPool: StakingPool,
        val currency: WalletCurrency
    ) : PickStakingOptionEvent()
    data class ShowPoolDetails(
        val service: StakingService,
        val pool: StakingPool,
        val currency: WalletCurrency
    ) : PickStakingOptionEvent()
}