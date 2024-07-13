package com.tonapps.tonkeeper.ui.screen.buysell

import uikit.mvi.UiState

data class BuySellScreenState(
    val headerTitle: CharSequence = "",
    val headerSubtitle: CharSequence? = null,
    val headerVisible: Boolean = true,
    val currentPage: Int = 0
) : UiState()