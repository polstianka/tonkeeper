package com.tonapps.tonkeeper.fragment.swap.domain.model

import android.os.Parcelable
import com.tonapps.wallet.api.entity.TokenEntity
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class DexAsset(
    val type: DexAssetType,
    val balance: BigDecimal,
    val rate: DexAssetRate
): Parcelable {
    val tokenEntity = rate.tokenEntity
    val decimals = tokenEntity.decimals
    val contractAddress = tokenEntity.address
    val imageUri = tokenEntity.imageUri
    val symbol = tokenEntity.symbol
    val displayName = tokenEntity.name
    val dexUsdPrice = rate.dexUsdPrice
}

@Parcelize
data class DexAssetRate(
    val tokenEntity: TokenEntity,
    val dexUsdPrice: BigDecimal
) : Parcelable

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
