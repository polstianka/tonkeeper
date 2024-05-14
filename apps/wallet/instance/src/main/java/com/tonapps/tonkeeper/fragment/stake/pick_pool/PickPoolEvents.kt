package com.tonapps.tonkeeper.fragment.stake.pick_pool

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService

sealed class PickPoolEvents {
    object NavigateBack : PickPoolEvents()
    object CloseFlow : PickPoolEvents()
    data class NavigateToPoolDetails(
        val service: StakingService,
        val pool: StakingPool
    ) : PickPoolEvents()
}