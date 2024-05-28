package com.tonapps.tonkeeper.fragment.swap

sealed interface SwapScreenEffect {

    data class ShowMessage(val text: String) : SwapScreenEffect
    data object CloseMessage : SwapScreenEffect
    data object Back : SwapScreenEffect
    data object OpenSettings : SwapScreenEffect
    data object Finish : SwapScreenEffect

}