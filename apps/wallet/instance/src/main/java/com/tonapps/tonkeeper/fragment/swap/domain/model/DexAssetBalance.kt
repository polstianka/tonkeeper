package com.tonapps.tonkeeper.fragment.swap.domain.model

import android.os.Parcelable
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.WalletCurrency
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
@Serializable
data class DexAssetRate(
    val tokenEntity: TokenEntity,
    val currency: WalletCurrency,
    @Serializable(MyBigDecimalSerializer::class)
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

private class MyBigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor
        get() = serialDescriptor<BigDecimal>()

    override fun deserialize(decoder: Decoder): BigDecimal {
        val string = decoder.decodeString()
        return BigDecimal(string)
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }
}
