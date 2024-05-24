package com.tonapps.tonkeeper.fragment.swap.confirm

sealed class ConfirmSwapEvent {

    object NavigateBack : ConfirmSwapEvent()
    data class FinishFlow(
        val navigateToHistory: Boolean
    ) : ConfirmSwapEvent()
}