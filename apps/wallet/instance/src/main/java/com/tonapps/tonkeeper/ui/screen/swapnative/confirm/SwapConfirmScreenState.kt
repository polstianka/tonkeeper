package com.tonapps.tonkeeper.ui.screen.swapnative.confirm

data class SwapConfirmScreenState(
    val isLoading : Boolean
) {

    companion object {
        val initState = SwapConfirmScreenState(isLoading = false)
    }

}
