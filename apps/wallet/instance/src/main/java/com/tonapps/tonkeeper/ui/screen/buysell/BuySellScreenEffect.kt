package com.tonapps.tonkeeper.ui.screen.buysell

import uikit.mvi.UiEffect

sealed class BuySellScreenEffect : UiEffect() {

    data object Finish : BuySellScreenEffect()

}