package com.tonapps.tonkeeper.fragment.swap.root

import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetType

sealed class SwapEvent {

    object NavigateBack : SwapEvent()
    data class NavigateToPickAsset(val type: PickAssetType) : SwapEvent()
    data class NavigateToSwapSettings(val settings: SwapSettings) : SwapEvent()
}