package com.tonapps.tonkeeper.fragment.stake.confirm

sealed class ConfirmStakeEvent {
    object NavigateBack : ConfirmStakeEvent()
    object CloseFlow : ConfirmStakeEvent()
}