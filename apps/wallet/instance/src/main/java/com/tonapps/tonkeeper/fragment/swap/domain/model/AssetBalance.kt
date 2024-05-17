package com.tonapps.tonkeeper.fragment.swap.domain.model

sealed class AssetBalance {
    object Loading : AssetBalance()
    data class Entity(
        val asset: DexAsset,
        val balance: Long
    ) : AssetBalance()
}