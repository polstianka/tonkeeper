package com.tonapps.tonkeeper.fragment.swap.domain.model

import android.os.Parcelable
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.stake.root.StakeViewModel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class DexAsset(
    val isCommunity: Boolean,
    val contractAddress: String,
    val decimals: Int,
    val hasDefaultSymbol: Boolean,
    val type: DexAssetType,
    val symbol: String,
    val imageUrl: String,
    val displayName: String,
    val dexUsdPrice: BigDecimal,
    val balance: BigDecimal
): Parcelable

enum class DexAssetType {
    JETTON,
    WTON,
    TON
}

fun DexAssetType.recommendedForwardTon(receiveType: DexAssetType): BigDecimal {
    return when {

        this == DexAssetType.TON &&
                receiveType == DexAssetType.JETTON -> BigDecimal("0.215")

        this == DexAssetType.JETTON &&
                receiveType == DexAssetType.JETTON -> BigDecimal("0.205")

        this == DexAssetType.JETTON &&
                receiveType == DexAssetType.TON -> BigDecimal("0.125")

        else -> throw IllegalStateException(
            "illegal exchange detected: $this -> $receiveType"
        )
    }
}

fun DexAsset.getRecommendedGasValues(receiveAsset: DexAsset): BigDecimal {
    return type.recommendedForwardTon(receiveAsset.type) + BigDecimal("0.06")
}
