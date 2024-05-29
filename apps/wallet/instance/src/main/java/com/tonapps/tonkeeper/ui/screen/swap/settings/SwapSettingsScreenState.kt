package com.tonapps.tonkeeper.ui.screen.swap.settings

import uikit.mvi.UiState

data class SwapSettingsScreenState(
    val expertMode: Boolean = false,
    val slippage: Float = 0.01f,
    val asText: Boolean = false,
    val saved: Boolean = false,
    val error: Boolean = false
) : UiState()