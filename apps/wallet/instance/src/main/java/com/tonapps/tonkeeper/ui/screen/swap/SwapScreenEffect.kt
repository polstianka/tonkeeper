package com.tonapps.tonkeeper.ui.screen.swap

import uikit.mvi.UiEffect

sealed class SwapScreenEffect : UiEffect() {

    data object Finish : SwapScreenEffect()

    data object FinishAndGoHistory: SwapScreenEffect()

}