package com.tonapps.tonkeeper.fragment.swap.pick_asset

import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance

sealed class PickAssetEvent {
    object NavigateBack : PickAssetEvent()
    data class ReturnResult(
        val asset: DexAssetBalance,
        val type: PickAssetType
    ) : PickAssetEvent()
}