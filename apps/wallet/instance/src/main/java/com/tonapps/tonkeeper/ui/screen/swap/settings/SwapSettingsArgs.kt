package com.tonapps.tonkeeper.ui.screen.swap.settings

import android.os.Bundle
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapSettings
import uikit.base.BaseArgs

data class SwapSettingsArgs (
    val requestKey: String,
    val currentSettings: SwapSettings = SwapSettings()
): BaseArgs() {

    private companion object {
        private const val ARG_REQUEST_KEY = "key"
        private const val ARG_CURRENT_SETTINGS = "settings"
    }

    constructor(bundle: Bundle) : this(
        requestKey = bundle.getString(ARG_REQUEST_KEY)!!,
        currentSettings = bundle.getParcelableCompat(ARG_CURRENT_SETTINGS) ?: SwapSettings()
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putString(ARG_REQUEST_KEY, requestKey)
        putParcelable(ARG_CURRENT_SETTINGS, currentSettings)
    }
}