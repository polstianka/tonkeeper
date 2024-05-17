package com.tonapps.tonkeeper.fragment.swap.pick_asset

import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset

sealed class PickAssetEvent {
    object NavigateBack : PickAssetEvent()
    data class ReturnResult(
        val asset: DexAsset,
        val type: PickAssetType
    ) : PickAssetEvent()
}