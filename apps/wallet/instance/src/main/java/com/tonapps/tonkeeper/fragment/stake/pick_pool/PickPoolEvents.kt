package com.tonapps.tonkeeper.fragment.stake.pick_pool

sealed class PickPoolEvents {
    object NavigateBack : PickPoolEvents()
    object CloseFlow : PickPoolEvents()
}