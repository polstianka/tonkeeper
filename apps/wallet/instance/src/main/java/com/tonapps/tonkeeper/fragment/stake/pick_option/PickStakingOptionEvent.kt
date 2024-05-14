package com.tonapps.tonkeeper.fragment.stake.pick_option

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool

sealed class PickStakingOptionEvent {
    object NavigateBack : PickStakingOptionEvent()
    object CloseFlow : PickStakingOptionEvent()
    data class ShowPoolPicker(
        val title: String,
        val pools: List<StakingPool>,
        val pickedPool: StakingPool
    ) : PickStakingOptionEvent()
}