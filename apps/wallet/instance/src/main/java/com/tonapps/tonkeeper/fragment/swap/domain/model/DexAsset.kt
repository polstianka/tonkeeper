package com.tonapps.tonkeeper.fragment.swap.domain.model

data class DexAsset(
    val isCommunity: Boolean,
    val contractAddress: String,
    val decimals: Int,
    val hasDefaultSymbol: Boolean,
    val type: DexAssetType,
    val symbol: String,
    val imageUrl: String,
    val displayName: String,

)

enum class DexAssetType {
    JETTON,
    WTON,
    TON
}
