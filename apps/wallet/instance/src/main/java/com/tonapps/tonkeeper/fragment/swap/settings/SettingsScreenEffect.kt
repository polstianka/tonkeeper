package com.tonapps.tonkeeper.fragment.swap.settings

sealed interface SettingsScreenEffect {
    data class UpdateSlippage(val value: String) : SettingsScreenEffect
    data object Finish : SettingsScreenEffect
}