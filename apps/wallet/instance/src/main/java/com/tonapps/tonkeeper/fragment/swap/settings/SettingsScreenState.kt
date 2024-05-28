package com.tonapps.tonkeeper.fragment.swap.settings

data class SettingsScreenState(
    val slippage: Float = 0f,
    val expertMode: Boolean = false,
    val suggestions: List<Int> = listOf(1, 3, 5),
)