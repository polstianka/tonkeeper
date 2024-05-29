package com.tonapps.tonkeeper.ui.screen.swap

import uikit.mvi.UiState

data class SwapScreenState(
    val headerTitle: CharSequence = "",
    val headerVisible: Boolean = true,
    val currentPage: Int = 0
) : UiState()