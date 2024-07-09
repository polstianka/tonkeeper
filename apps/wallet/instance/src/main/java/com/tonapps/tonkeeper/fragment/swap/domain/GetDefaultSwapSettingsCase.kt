package com.tonapps.tonkeeper.fragment.swap.domain

import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings

class GetDefaultSwapSettingsCase {

    fun execute(): SwapSettings = SwapSettings.NoviceMode.One
}