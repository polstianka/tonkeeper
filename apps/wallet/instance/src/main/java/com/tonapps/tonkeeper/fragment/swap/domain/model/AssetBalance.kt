package com.tonapps.tonkeeper.fragment.swap.domain.model

import java.math.BigDecimal

sealed class AssetBalance {
    object Loading : AssetBalance()
    data class Entity(
        val asset: DexAsset,
        val balance: BigDecimal
    ) : AssetBalance()
}