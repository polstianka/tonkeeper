package com.tonapps.tonkeeper.fragment.stake.balance

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingDirection
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService

sealed class StakedBalanceEvent {
    object NavigateBack : StakedBalanceEvent()
    data class NavigateToStake(
        val pool: StakingPool,
        val service: StakingService,
        val stakingDirection: StakingDirection
    ) : StakedBalanceEvent()
    data class NavigateToLink(val url: String) : StakedBalanceEvent()
}