package com.tonapps.tonkeeper.fragment.swap.settings

sealed class SwapSettingsEvent {

    object NavigateBack : SwapSettingsEvent()
    data class FillInput(val text: String) : SwapSettingsEvent()
}