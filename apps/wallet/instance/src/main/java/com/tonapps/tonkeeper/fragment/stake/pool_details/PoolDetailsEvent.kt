package com.tonapps.tonkeeper.fragment.stake.pool_details

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool

sealed class PoolDetailsEvent {
    object NavigateBack : PoolDetailsEvent()
    object FinishFlow : PoolDetailsEvent()
    data class NavigateToLink(val url: String) : PoolDetailsEvent()
    data class PickPool(val pool: StakingPool) : PoolDetailsEvent()
}