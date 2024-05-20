package com.tonapps.tonkeeper.fragment.swap.root

import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetType
import java.math.BigDecimal

sealed class SwapEvent {

    object NavigateBack : SwapEvent()
    data class FillInput(val text: String) : SwapEvent()
    data class NavigateToPickAsset(val type: PickAssetType) : SwapEvent()
    data class NavigateToSwapSettings(val settings: SwapSettings) : SwapEvent()
    data class NavigateToConfirm(
        val sendAsset: DexAsset,
        val receiveAsset: DexAsset,
        val settings: SwapSettings,
        val amount: BigDecimal,
        val simulation: SwapSimulation.Result
    ) : SwapEvent()
}