package com.tonapps.tonkeeper.ui.screen.stake

import uikit.mvi.UiEffect

sealed class StakeScreenEffect : UiEffect() {

    //data object OpenCamera: SendScreenEffect()

    data object Finish : StakeScreenEffect()

}