package com.tonapps.tonkeeper.fragment.stake.pick_option

sealed class PickStakingOptionEvent {
    object NavigateBack : PickStakingOptionEvent()
    object CloseFlow : PickStakingOptionEvent()
}