package com.tonapps.tonkeeper.fragment.swap.confirm

sealed class ConfirmSwapEvent {

    object CloseFlow : ConfirmSwapEvent()
}