package com.tonapps.tonkeeper.fragment.swap.settings

import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings

sealed class SwapSettingsEvent {

    object NavigateBack : SwapSettingsEvent()
    data class FillInput(val text: String) : SwapSettingsEvent()
    data class ReturnResult(val settings: SwapSettings) : SwapSettingsEvent()
}