package com.tonapps.tonkeeper.fragment.stake.pick_pool

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.wallet.data.core.WalletCurrency

sealed class PickPoolEvents {
    object NavigateBack : PickPoolEvents()
    object CloseFlow : PickPoolEvents()
    data class NavigateToPoolDetails(
        val service: StakingService,
        val pool: StakingPool,
        val currency: WalletCurrency
    ) : PickPoolEvents()
}