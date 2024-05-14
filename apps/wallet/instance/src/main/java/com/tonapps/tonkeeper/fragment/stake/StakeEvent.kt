package com.tonapps.tonkeeper.fragment.stake

sealed class StakeEvent {
    object NavigateBack : StakeEvent()
    object ShowInfo : StakeEvent()
    data class SetInputValue(val value: Float) : StakeEvent()
}