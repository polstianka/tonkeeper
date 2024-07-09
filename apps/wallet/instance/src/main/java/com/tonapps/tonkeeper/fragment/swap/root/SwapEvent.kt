package com.tonapps.tonkeeper.fragment.swap.root

import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetType
import com.tonapps.wallet.api.entity.TokenEntity
import java.math.BigDecimal

sealed class SwapEvent {

    object NavigateBack : SwapEvent()
    data class FillInput(val text: String) : SwapEvent()
    data class NavigateToPickAsset(
        val type: PickAssetType,
        val toSend: TokenEntity?,
        val toReceive: TokenEntity?
    ) : SwapEvent()
    data class NavigateToSwapSettings(val settings: SwapSettings) : SwapEvent()
    data class NavigateToConfirm(
        val sendAsset: DexAssetBalance,
        val receiveAsset: DexAssetBalance,
        val settings: SwapSettings,
        val amount: BigDecimal,
        val simulation: SwapSimulation.Result
    ) : SwapEvent()
}