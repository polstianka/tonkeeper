package com.tonapps.tonkeeper.ui.screen.swapnative.main

import androidx.annotation.StringRes


sealed class SwapNativeScreenEffect {

    data class Finish(@StringRes val toast: Int?) : SwapNativeScreenEffect()

}