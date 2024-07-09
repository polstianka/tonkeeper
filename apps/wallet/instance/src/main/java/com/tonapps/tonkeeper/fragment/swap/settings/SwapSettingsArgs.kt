package com.tonapps.tonkeeper.fragment.swap.settings

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeper.fragment.swap.domain.model.toBundle
import com.tonapps.tonkeeper.fragment.swap.domain.model.toSwapSettings
import uikit.base.BaseArgs

data class SwapSettingsArgs(
    val settings: SwapSettings
): BaseArgs() {
    override fun toBundle(): Bundle {
        return settings.toBundle()
    }
    
    constructor(bundle: Bundle) : this(
        settings = bundle.toSwapSettings()
    )
}