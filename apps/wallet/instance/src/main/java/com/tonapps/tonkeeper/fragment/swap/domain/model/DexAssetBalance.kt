package com.tonapps.tonkeeper.fragment.swap.domain.model

import android.os.Parcelable
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.WalletCurrency
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class DexAssetBalance(
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
}

@Parcelize
data class DexAssetRate(
    val tokenEntity: TokenEntity,
    val currency: WalletCurrency,
    val rate: BigDecimal
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

fun DexAssetBalance.getRecommendedGasValues(receiveAsset: DexAssetBalance): BigDecimal {
    return type.recommendedForwardTon(receiveAsset.type) + BigDecimal("0.06")
}
