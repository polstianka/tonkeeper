package com.tonapps.tonkeeper.fragment.swap

sealed interface SwapScreenState {

    data object Loading : SwapScreenState

    data object Error : SwapScreenState

    data class Content(
        val headerTitle: CharSequence = "",
        val headerVisible: Boolean = true,
        val currentPage: Int = 0,
    ) : SwapScreenState
}