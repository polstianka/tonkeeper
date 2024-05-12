package com.tonapps.tonkeeper.fragment.trade.pick_operator

sealed class PickOperatorEvents {
    object NavigateBack : PickOperatorEvents()
    object CloseFlow : PickOperatorEvents()
}