package com.tonapps.tonkeeper.ui.screen.stake

import uikit.mvi.UiState

data class StakeScreenState(
    val headerTitle: CharSequence = "",
    val headerVisible: Boolean = true,
    val currentPage: Int = 0
) : UiState()