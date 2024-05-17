package com.tonapps.tonkeeper.fragment.swap.pick_asset

sealed class PickAssetEvent {
    object NavigateBack : PickAssetEvent()
}