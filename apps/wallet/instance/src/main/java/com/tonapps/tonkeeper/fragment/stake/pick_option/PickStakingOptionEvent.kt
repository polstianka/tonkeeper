package com.tonapps.tonkeeper.fragment.stake.pick_option

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService

sealed class PickStakingOptionEvent {
    object NavigateBack : PickStakingOptionEvent()
    object CloseFlow : PickStakingOptionEvent()
    data class ShowPoolPicker(
        val service: StakingService,
        val pickedPool: StakingPool
    ) : PickStakingOptionEvent()
    data class ShowPoolDetails(
        val service: StakingService,
        val pool: StakingPool
    ) : PickStakingOptionEvent()
}